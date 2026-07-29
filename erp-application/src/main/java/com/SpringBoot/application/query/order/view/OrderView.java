package com.SpringBoot.application.query.order.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderView(
        UUID id,
        String orderNumber,
        Long customerId,
        String customerName,
        String status,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime orderDate,
        String cancellationReason,
        List<OrderItemView> items
) {
}
