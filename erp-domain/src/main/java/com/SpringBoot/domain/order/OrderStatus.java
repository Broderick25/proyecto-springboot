package com.SpringBoot.domain.order;

import java.util.Map;
import java.util.Set;

public record OrderStatus(String value) {

    private static final Set<String> VALID_VALUES =
            Set.of("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");

    private static final Set<String> FINAL_STATES = Set.of("DELIVERED", "CANCELLED");

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "PENDING", Set.of("CONFIRMED", "CANCELLED"),
            "CONFIRMED", Set.of("SHIPPED", "CANCELLED"),
            "SHIPPED", Set.of("DELIVERED"),
            "DELIVERED", Set.of(),
            "CANCELLED", Set.of()
    );

    public OrderStatus {
        if (value == null) {
            throw new IllegalArgumentException("OrderStatus value must not be null");
        }
        if (!VALID_VALUES.contains(value)) {
            throw new IllegalArgumentException("Invalid OrderStatus value: " + value);
        }
    }

    public static OrderStatus of(String value) {
        return new OrderStatus(value);
    }

    public static OrderStatus pending() {
        return new OrderStatus("PENDING");
    }

    public static OrderStatus confirmed() {
        return new OrderStatus("CONFIRMED");
    }

    public static OrderStatus shipped() {
        return new OrderStatus("SHIPPED");
    }

    public static OrderStatus delivered() {
        return new OrderStatus("DELIVERED");
    }

    public static OrderStatus cancelled() {
        return new OrderStatus("CANCELLED");
    }

    public boolean canTransitionTo(OrderStatus nextStatus) {
        if (nextStatus == null) {
            throw new IllegalArgumentException("nextStatus must not be null");
        }
        return TRANSITIONS.get(this.value).contains(nextStatus.value);
    }

    public boolean isPending() {
        return "PENDING".equals(value);
    }

    public boolean isConfirmed() {
        return "CONFIRMED".equals(value);
    }

    public boolean isShipped() {
        return "SHIPPED".equals(value);
    }

    public boolean isDelivered() {
        return "DELIVERED".equals(value);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(value);
    }

    public boolean isFinalState() {
        return FINAL_STATES.contains(value);
    }
}
