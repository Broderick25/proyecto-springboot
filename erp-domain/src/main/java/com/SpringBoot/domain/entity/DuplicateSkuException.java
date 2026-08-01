package com.SpringBoot.domain.entity;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String sku) {
        super("Ya existe un producto con el SKU " + sku);
    }

    public DuplicateSkuException(String sku, Throwable cause) {
        super("Ya existe un producto con el SKU " + sku, cause);
    }
}
