package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.application.command.product.ProductMapper;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.order.Order;
import com.SpringBoot.domain.order.OrderItem;
import com.SpringBoot.domain.product.ProductId;
import com.SpringBoot.domain.repository.OrderRepository;
import com.SpringBoot.domain.repository.ProductRepository;
import com.SpringBoot.domain.shared.Quantity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UpdateOrderCommandHandler implements CommandHandler<UpdateOrderCommand, Void> {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UpdateOrderCommandHandler(OrderRepository orderRepository, ProductRepository productRepository,
                                      ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void handle(UpdateOrderCommand command) {
        com.SpringBoot.domain.entity.Order entity = orderRepository.findByIdWithItems(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        Order aggregate = OrderMapper.toAggregate(entity);

        // Falla rápido si la orden ya no admite cambios, antes de resolver productos: evita
        // consultas innecesarias y, sobre todo, que un ProductNotFoundException tape el error
        // real (la orden no está en PENDING). Order.updateItems() vuelve a validar esto mismo
        // más abajo — sigue siendo la fuente de verdad de la regla.
        if (!aggregate.getStatus().isPending()) {
            throw new IllegalStateException(
                    "Cannot update items of an order in status " + aggregate.getStatus().value()
                            + "; only PENDING allowed");
        }

        List<OrderItem> newItems = new ArrayList<>();
        Map<ProductId, com.SpringBoot.domain.entity.Product> productsById = new LinkedHashMap<>();

        for (UpdateOrderCommand.OrderLine line : command.items()) {
            com.SpringBoot.domain.entity.Product productEntity = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ProductNotFoundException(line.productId()));

            com.SpringBoot.domain.product.Product productAggregate = ProductMapper.toAggregate(productEntity);
            newItems.add(OrderItem.from(productAggregate, Quantity.of(line.quantity())));
            productsById.put(productAggregate.getId(), productEntity);
        }

        aggregate.updateItems(newItems);

        OrderMapper.syncItems(aggregate, entity, productsById);
        orderRepository.save(entity);

        aggregate.getDomainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();

        return null;
    }
}
