package com.SpringBoot.domain.entity;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID productId) {
        super("No existe un producto con id " + productId);
    }
}
