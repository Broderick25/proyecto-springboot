package com.SpringBoot.domain.product;

public record Stock(Integer value) {

    public Stock {
        if (value == null) {
            throw new IllegalArgumentException("Stock value must not be null");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Stock value must not be negative");
        }
    }

    public static Stock of(int value) {
        return new Stock(value);
    }

    public static Stock zero() {
        return new Stock(0);
    }

    public Stock increment(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("increment quantity must not be negative");
        }
        return new Stock(this.value + quantity);
    }

    public Stock decrement(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("decrement quantity must not be negative");
        }
        if (quantity > this.value) {
            throw new IllegalArgumentException("Cannot decrement stock below zero");
        }
        return new Stock(this.value - quantity);
    }

    public boolean hasAvailable(int required) {
        return this.value >= required;
    }
}
