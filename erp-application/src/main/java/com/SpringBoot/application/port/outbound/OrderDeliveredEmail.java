package com.SpringBoot.application.port.outbound;

public record OrderDeliveredEmail(String to, String customerName, String orderNumber) {
}
