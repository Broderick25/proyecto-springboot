package com.SpringBoot.domain.order.events;

import com.SpringBoot.domain.common.DomainEvent;
import com.SpringBoot.domain.order.OrderId;
import com.SpringBoot.domain.shared.CustomerId;
import com.SpringBoot.domain.shared.Money;

import java.time.Instant;

public record OrderCreated(OrderId orderId, CustomerId customerId, String customerName, Money totalAmount,
                            Instant timestamp) implements DomainEvent {

    public OrderCreated {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("customerName must not be null or blank");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("totalAmount must not be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null");
        }
    }
}
