package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.order.Order;
import com.SpringBoot.domain.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliverOrderCommandHandler implements CommandHandler<DeliverOrderCommand, Void> {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DeliverOrderCommandHandler(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void handle(DeliverOrderCommand command) {
        com.SpringBoot.domain.entity.Order entity = orderRepository.findByIdWithItems(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        Order aggregate = OrderMapper.toAggregate(entity);
        aggregate.deliver();

        OrderMapper.copyToEntity(aggregate, entity);
        orderRepository.save(entity);

        aggregate.getDomainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();

        return null;
    }
}
