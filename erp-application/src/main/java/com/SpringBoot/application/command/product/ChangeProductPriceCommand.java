package com.SpringBoot.application.command.product;

import com.SpringBoot.application.command.Command;

import java.math.BigDecimal;
import java.util.UUID;

public record ChangeProductPriceCommand(UUID productId, BigDecimal newPrice) implements Command {
}
