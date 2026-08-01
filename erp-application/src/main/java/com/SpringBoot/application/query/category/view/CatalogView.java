package com.SpringBoot.application.query.category.view;

import java.time.Instant;
import java.util.List;

public record CatalogView(
        String id,
        String catalogType,
        String name,
        String description,
        boolean active,
        List<ItemView> items,
        Instant createdAt,
        Instant updatedAt
) {
}
