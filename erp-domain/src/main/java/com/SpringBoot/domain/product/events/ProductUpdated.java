package com.SpringBoot.domain.product.events;

import com.SpringBoot.domain.common.DomainEvent;
import com.SpringBoot.domain.product.ProductId;

import java.time.Instant;

public record ProductUpdated(ProductId productId, Instant timestamp) implements DomainEvent {

    public ProductUpdated {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null");
        }
    }
}
