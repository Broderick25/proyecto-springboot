package com.SpringBoot.application.port.outbound;

public record OrderShippedEmail(String to, String customerName, String orderNumber) {
}
