package com.SpringBoot.application.query.order.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderSummaryView(
        UUID id,
        String orderNumber,
        Long customerId,
        String customerName,
        String status,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime orderDate
) {
}
