package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.application.command.product.DecrementProductStockCommand;
import com.SpringBoot.application.command.product.DecrementProductStockCommandHandler;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.order.Order;
import com.SpringBoot.domain.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmOrderCommandHandler implements CommandHandler<ConfirmOrderCommand, Void> {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DecrementProductStockCommandHandler decrementProductStockCommandHandler;

    public ConfirmOrderCommandHandler(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher,
                                       DecrementProductStockCommandHandler decrementProductStockCommandHandler) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.decrementProductStockCommandHandler = decrementProductStockCommandHandler;
    }

    @Override
    @Transactional
    public Void handle(ConfirmOrderCommand command) {
        com.SpringBoot.domain.entity.Order entity = orderRepository.findByIdWithItems(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        Order aggregate = OrderMapper.toAggregate(entity);
        aggregate.confirm();

        // Reserva el stock de cada línea al confirmar — necesario para que CancelOrderCommand
        // pueda liberarlo correctamente si la orden se cancela más adelante.
        aggregate.getItems().forEach(item -> decrementProductStockCommandHandler.handle(
                new DecrementProductStockCommand(item.getProductReference().value(), item.getQuantity().value(),
                        "Confirmación de orden " + aggregate.getOrderNumber().value())));

        OrderMapper.copyToEntity(aggregate, entity);
        orderRepository.save(entity);

        aggregate.getDomainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();

        return null;
    }
}
