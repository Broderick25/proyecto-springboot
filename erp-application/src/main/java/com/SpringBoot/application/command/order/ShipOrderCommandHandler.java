package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.order.Order;
import com.SpringBoot.domain.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipOrderCommandHandler implements CommandHandler<ShipOrderCommand, Void> {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ShipOrderCommandHandler(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void handle(ShipOrderCommand command) {
        com.SpringBoot.domain.entity.Order entity = orderRepository.findByIdWithItems(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        Order aggregate = OrderMapper.toAggregate(entity);
        aggregate.ship();

        OrderMapper.copyToEntity(aggregate, entity);
        orderRepository.save(entity);

        aggregate.getDomainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();

        return null;
    }
}
