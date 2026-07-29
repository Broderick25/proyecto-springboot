package com.SpringBoot.domain.document;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String categoryId) {
        super("No existe una categoría con id " + categoryId);
    }
}
