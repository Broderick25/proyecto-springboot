package com.SpringBoot.domain.product;

import java.util.regex.Pattern;

public record SKU(String value) {

    private static final Pattern PATTERN = Pattern.compile("[A-Z]+-\\d{3}");

    public SKU {
        if (value == null) {
            throw new IllegalArgumentException("SKU value must not be null");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("SKU must match pattern [A-Z]+-NNN, e.g. LAPTOP-001: " + value);
        }
    }

    public static SKU of(String value) {
        return new SKU(value);
    }
}
