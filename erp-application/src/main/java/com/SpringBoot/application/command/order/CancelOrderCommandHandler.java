package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.application.command.product.IncrementProductStockCommand;
import com.SpringBoot.application.command.product.IncrementProductStockCommandHandler;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.order.Order;
import com.SpringBoot.domain.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelOrderCommandHandler implements CommandHandler<CancelOrderCommand, Void> {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IncrementProductStockCommandHandler incrementProductStockCommandHandler;

    public CancelOrderCommandHandler(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher,
                                      IncrementProductStockCommandHandler incrementProductStockCommandHandler) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.incrementProductStockCommandHandler = incrementProductStockCommandHandler;
    }

    @Override
    @Transactional
    public Void handle(CancelOrderCommand command) {
        com.SpringBoot.domain.entity.Order entity = orderRepository.findByIdWithItems(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        Order aggregate = OrderMapper.toAggregate(entity);
        // Capturar ANTES de cancel(): después de cancelar el status ya es CANCELLED, y con eso
        // perderíamos la información de si la orden alguna vez llegó a reservar stock.
        boolean releaseStock = aggregate.getStatus().isConfirmed();

        aggregate.cancel(command.reason());

        OrderMapper.copyToEntity(aggregate, entity);
        entity.setCancellationReason(command.reason());
        orderRepository.save(entity);

        if (releaseStock) {
            aggregate.getItems().forEach(item -> incrementProductStockCommandHandler.handle(
                    new IncrementProductStockCommand(item.getProductReference().value(), item.getQuantity().value(),
                            "Cancelación de orden " + aggregate.getOrderNumber().value())));
        }

        aggregate.getDomainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();

        return null;
    }
}
