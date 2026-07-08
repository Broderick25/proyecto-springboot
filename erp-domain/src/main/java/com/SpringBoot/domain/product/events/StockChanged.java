package com.SpringBoot.domain.product.events;

import com.SpringBoot.domain.common.DomainEvent;
import com.SpringBoot.domain.product.ProductId;

import java.time.Instant;

public record StockChanged(ProductId productId, Integer oldStock, Integer newStock, String reason,
                            Instant timestamp) implements DomainEvent {

    public StockChanged {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (oldStock == null) {
            throw new IllegalArgumentException("oldStock must not be null");
        }
        if (newStock == null) {
            throw new IllegalArgumentException("newStock must not be null");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be null or blank");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null");
        }
    }
}
