package com.SpringBoot.domain.document;

public class DuplicateCategoryException extends RuntimeException {

    public DuplicateCategoryException(String categoryId) {
        super("Ya existe una categoría con el id " + categoryId);
    }
}
