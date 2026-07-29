package com.SpringBoot.application.query.order.view;

import java.math.BigDecimal;

public record OrderItemView(String productName, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
}
