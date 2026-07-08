package com.SpringBoot.domain.shared;

public record CustomerId(Long value) {

    public CustomerId {
        if (value == null) {
            throw new IllegalArgumentException("CustomerId value must not be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("CustomerId value must be greater than 0");
        }
    }

    public static CustomerId of(Long value) {
        return new CustomerId(value);
    }
}
