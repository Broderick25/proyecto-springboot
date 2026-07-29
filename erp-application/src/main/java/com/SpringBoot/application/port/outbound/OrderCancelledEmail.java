package com.SpringBoot.application.port.outbound;

public record OrderCancelledEmail(String to, String customerName, String orderNumber, String reason) {
}
