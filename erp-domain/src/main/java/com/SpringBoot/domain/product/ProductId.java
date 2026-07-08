package com.SpringBoot.domain.product;

import java.util.UUID;

public record ProductId(UUID value) {

    public ProductId {
        if (value == null) {
            throw new IllegalArgumentException("ProductId value must not be null");
        }
    }

    public static ProductId of(UUID value) {
        return new ProductId(value);
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }
}
