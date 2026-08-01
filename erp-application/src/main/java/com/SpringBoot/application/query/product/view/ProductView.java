package com.SpringBoot.application.query.product.view;

import com.SpringBoot.domain.document.ProductSpecifications;

import java.math.BigDecimal;
import java.util.List;

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
        boolean active,
        List<String> tags,
        ProductSpecifications specifications
) {
}
