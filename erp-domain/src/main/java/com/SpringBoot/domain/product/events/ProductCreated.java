package com.SpringBoot.domain.product.events;

import com.SpringBoot.domain.common.DomainEvent;
import com.SpringBoot.domain.product.ProductId;
import com.SpringBoot.domain.product.ProductName;
import com.SpringBoot.domain.product.SKU;
import com.SpringBoot.domain.shared.Money;

import java.time.Instant;

public record ProductCreated(ProductId productId, SKU sku, ProductName name, Money price,
                              Instant timestamp) implements DomainEvent {

    public ProductCreated {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (sku == null) {
            throw new IllegalArgumentException("sku must not be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (price == null) {
            throw new IllegalArgumentException("price must not be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null");
        }
    }
}
