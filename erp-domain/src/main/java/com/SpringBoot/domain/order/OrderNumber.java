package com.SpringBoot.domain.order;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public record OrderNumber(String value) {

    private static final Pattern PATTERN = Pattern.compile("ORD-\\d{4}-\\d{3}");

    public OrderNumber {
        if (value == null) {
            throw new IllegalArgumentException("OrderNumber value must not be null");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("OrderNumber must match pattern ORD-YYYY-NNN: " + value);
        }
    }

    public static OrderNumber of(String value) {
        return new OrderNumber(value);
    }

    public static OrderNumber generate() {
        int year = Year.now().getValue();
        int sequence = ThreadLocalRandom.current().nextInt(1, 1000);
        return new OrderNumber(String.format("ORD-%d-%03d", year, sequence));
    }
}
