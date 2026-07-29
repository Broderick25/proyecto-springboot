package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.application.command.product.ProductMapper;
import com.SpringBoot.domain.customer.CustomerInfo;
import com.SpringBoot.domain.customer.CustomerNotFoundException;
import com.SpringBoot.domain.customer.CustomerProvider;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.order.Customer;
import com.SpringBoot.domain.order.Order;
import com.SpringBoot.domain.order.OrderItem;
import com.SpringBoot.domain.order.OrderNumber;
import com.SpringBoot.domain.product.ProductId;
import com.SpringBoot.domain.repository.OrderRepository;
import com.SpringBoot.domain.repository.ProductRepository;
import com.SpringBoot.domain.shared.CustomerId;
import com.SpringBoot.domain.shared.Quantity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, UUID> {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerProvider customerProvider;
    private final ApplicationEventPublisher eventPublisher;

    public CreateOrderCommandHandler(OrderRepository orderRepository, ProductRepository productRepository,
                                      CustomerProvider customerProvider, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerProvider = customerProvider;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UUID handle(CreateOrderCommand command) {
        CustomerInfo customerInfo = customerProvider.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));

        List<OrderItem> items = new ArrayList<>();
        Map<ProductId, com.SpringBoot.domain.entity.Product> productsById = new LinkedHashMap<>();

        for (CreateOrderCommand.OrderLine line : command.items()) {
            com.SpringBoot.domain.entity.Product productEntity = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ProductNotFoundException(line.productId()));

            com.SpringBoot.domain.product.Product productAggregate = ProductMapper.toAggregate(productEntity);
            items.add(OrderItem.from(productAggregate, Quantity.of(line.quantity())));
            productsById.put(productAggregate.getId(), productEntity);
        }

        Order aggregate = Order.create(
                OrderNumber.generate(),
                new Customer(CustomerId.of(command.customerId()), customerInfo.name()),
                items,
                command.createdBy());

        orderRepository.save(OrderMapper.toNewEntity(aggregate, productsById));

        aggregate.getDomainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();

        return aggregate.getId().value();
    }
}
