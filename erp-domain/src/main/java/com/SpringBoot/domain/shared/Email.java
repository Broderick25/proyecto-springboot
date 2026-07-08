package com.SpringBoot.domain.shared;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email value must not be null or blank");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Email value is not a valid email address: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }
}
