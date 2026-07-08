package com.SpringBoot.domain.product;

public record CategoryReference(String categoryId) {

    public CategoryReference {
        if (categoryId == null || categoryId.isBlank()) {
            throw new IllegalArgumentException("CategoryReference categoryId must not be null or blank");
        }
    }

    public static CategoryReference of(String categoryId) {
        return new CategoryReference(categoryId);
    }
}
