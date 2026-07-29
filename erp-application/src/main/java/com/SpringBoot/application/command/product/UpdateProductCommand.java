package com.SpringBoot.application.command.product;

import com.SpringBoot.application.command.Command;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductCommand(
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        String categoryId,
        String imageUrl
) implements Command {
}
