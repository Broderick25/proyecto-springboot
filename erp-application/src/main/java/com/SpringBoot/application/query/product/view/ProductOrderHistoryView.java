package com.SpringBoot.application.query.product.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductOrderHistoryView(
        UUID orderId,
        String orderNumber,
        LocalDateTime orderDate,
        String orderStatus,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
