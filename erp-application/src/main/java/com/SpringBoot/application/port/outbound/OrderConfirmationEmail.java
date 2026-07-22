package com.SpringBoot.application.port.outbound;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderConfirmationEmail(
        String to,
        String customerName,
        String orderNumber,
        String orderId,
        LocalDate orderDate,
        int itemsCount,
        String currency,
        BigDecimal totalAmount
) {
}
