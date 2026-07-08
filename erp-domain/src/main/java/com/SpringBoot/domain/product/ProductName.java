package com.SpringBoot.domain.product;

public record ProductName(String value) {

    public ProductName {
        if (value == null) {
            throw new IllegalArgumentException("ProductName value must not be null");
        }
        if (value.length() < 3 || value.length() > 200) {
            throw new IllegalArgumentException("ProductName length must be between 3 and 200 characters");
        }
    }

    public static ProductName of(String value) {
        return new ProductName(value);
    }
}
