package com.SpringBoot.application.query.order;

import com.SpringBoot.application.query.PageView;
import com.SpringBoot.application.query.QueryHandler;
import com.SpringBoot.application.query.order.view.OrderSummaryView;
import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListOrdersQueryHandler implements QueryHandler<ListOrdersQuery, PageView<OrderSummaryView>> {

    private static final int MAX_PAGE_SIZE = 100;

    private final OrderRepository orderRepository;

    public ListOrdersQueryHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public PageView<OrderSummaryView> handle(ListOrdersQuery query) {
        if (query.size() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must not exceed " + MAX_PAGE_SIZE);
        }

        OrderStatus status = parseStatus(query.status());
        Pageable pageable = PageRequest.of(query.page(), query.size());

        Page<Order> page = query.customerId() != null
                ? status != null
                        ? orderRepository.findByCustomerIdAndStatus(query.customerId(), status, pageable)
                        : orderRepository.findByCustomerId(query.customerId(), pageable)
                : status != null
                        ? orderRepository.findByStatus(status, pageable)
                        : orderRepository.findAll(pageable);

        return new PageView<>(
                page.getContent().stream().map(ListOrdersQueryHandler::toSummaryView).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private static OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("status inválido: " + status);
        }
    }

    private static OrderSummaryView toSummaryView(Order order) {
        return new OrderSummaryView(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getCustomerName(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getOrderDate());
    }
}
