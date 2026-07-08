package com.SpringBoot.domain.order;

import java.util.UUID;

public record OrderId(UUID value) {

    public OrderId {
        if (value == null) {
            throw new IllegalArgumentException("OrderId value must not be null");
        }
    }

    public static OrderId of(UUID value) {
        return new OrderId(value);
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }
}
