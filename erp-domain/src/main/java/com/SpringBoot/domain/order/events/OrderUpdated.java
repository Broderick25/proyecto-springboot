package com.SpringBoot.domain.order.events;

import com.SpringBoot.domain.common.DomainEvent;
import com.SpringBoot.domain.order.OrderId;

import java.time.Instant;

public record OrderUpdated(OrderId orderId, Instant timestamp) implements DomainEvent {

    public OrderUpdated {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null");
        }
    }
}
