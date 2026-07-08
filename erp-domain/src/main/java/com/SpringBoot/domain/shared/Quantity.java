package com.SpringBoot.domain.shared;

public record Quantity(Integer value) {

    public Quantity {
        if (value == null) {
            throw new IllegalArgumentException("Quantity value must not be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("Quantity value must be greater than 0");
        }
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }
}
