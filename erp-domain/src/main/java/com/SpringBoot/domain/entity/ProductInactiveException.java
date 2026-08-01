package com.SpringBoot.domain.entity;

import java.util.UUID;

public class ProductInactiveException extends RuntimeException {

    public ProductInactiveException(UUID productId) {
        super("El producto " + productId + " está desactivado");
    }
}
