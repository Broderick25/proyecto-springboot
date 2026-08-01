package com.SpringBoot.application.query.category.view;

public record ItemView(
        String id,
        String code,
        String value,
        String description,
        Integer displayOrder
) {
}
