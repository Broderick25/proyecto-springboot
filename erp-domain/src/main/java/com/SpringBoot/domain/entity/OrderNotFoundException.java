package com.SpringBoot.domain.entity;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID orderId) {
        super("No existe una orden con id " + orderId);
    }
}
