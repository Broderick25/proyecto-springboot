package com.SpringBoot.domain.document;

public class CategoryNotFoundException extends RuntimeException {

    private CategoryNotFoundException(String message) {
        super(message);
    }

    public static CategoryNotFoundException byId(String categoryId) {
        return new CategoryNotFoundException("No existe una categoría con id " + categoryId);
    }

    public static CategoryNotFoundException byCode(String code) {
        return new CategoryNotFoundException("No existe una categoría con code " + code);
    }
}
