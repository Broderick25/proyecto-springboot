package com.SpringBoot.domain.order.events;

import com.SpringBoot.domain.common.DomainEvent;
import com.SpringBoot.domain.order.OrderId;

import java.time.Instant;

public record OrderCancelled(OrderId orderId, String reason, Instant timestamp) implements DomainEvent {

    public OrderCancelled {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be null or blank");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null");
        }
    }
}
