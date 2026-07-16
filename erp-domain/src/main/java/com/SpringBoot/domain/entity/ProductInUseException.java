package com.SpringBoot.domain.entity;

import java.util.UUID;

public class ProductInUseException extends RuntimeException {

    public ProductInUseException(UUID productId, Throwable cause) {
        super("No se puede eliminar el producto " + productId + " porque tiene pedidos asociados", cause);
    }
}
