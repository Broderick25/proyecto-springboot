package com.SpringBoot.domain.document;

public class CategoryInUseException extends RuntimeException {

    public CategoryInUseException(String categoryId) {
        super("No se puede eliminar la categoría " + categoryId + " porque tiene productos asociados");
    }
}
