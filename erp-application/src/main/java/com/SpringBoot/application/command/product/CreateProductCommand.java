package com.SpringBoot.application.command.product;

import com.SpringBoot.application.command.Command;

import java.math.BigDecimal;

public record CreateProductCommand(
        String sku,
        String name,
        String description,
        BigDecimal price,
        int initialStock,
        String categoryId,
        String imageUrl,
        String createdBy
) implements Command {
}
