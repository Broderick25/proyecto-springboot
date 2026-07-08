package com.SpringBoot.domain.order;

import java.util.UUID;

public record OrderItemId(UUID value) {

    public OrderItemId {
        if (value == null) {
            throw new IllegalArgumentException("OrderItemId value must not be null");
        }
    }

    public static OrderItemId of(UUID value) {
        return new OrderItemId(value);
    }

    public static OrderItemId generate() {
        return new OrderItemId(UUID.randomUUID());
    }
}
