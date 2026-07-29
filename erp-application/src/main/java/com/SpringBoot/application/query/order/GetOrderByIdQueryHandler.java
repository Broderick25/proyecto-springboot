package com.SpringBoot.application.query.order;

import com.SpringBoot.application.query.QueryHandler;
import com.SpringBoot.application.query.order.view.OrderItemView;
import com.SpringBoot.application.query.order.view.OrderView;
import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class GetOrderByIdQueryHandler implements QueryHandler<GetOrderByIdQuery, OrderView> {

    private final OrderRepository orderRepository;

    public GetOrderByIdQueryHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderView handle(GetOrderByIdQuery query) {
        Order entity = orderRepository.findByIdWithItems(query.orderId())
                .orElseThrow(() -> new OrderNotFoundException(query.orderId()));

        return new OrderView(
                entity.getId(),
                entity.getOrderNumber(),
                entity.getCustomerId(),
                entity.getCustomerName(),
                entity.getStatus().name(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getOrderDate(),
                entity.getCancellationReason(),
                entity.getOrderProducts().stream()
                        .map(op -> new OrderItemView(op.getProductName(), op.getQuantity(), op.getUnitPrice(),
                                op.getSubtotal()))
                        .toList());
    }
}
