package com.SpringBoot.application.query.product.view;

import java.math.BigDecimal;

public record ProductView(
        String id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        Integer stock,
        String categoryId,
        String categoryName,
        String imageUrl,
        boolean active
) {
}
