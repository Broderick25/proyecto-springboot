package com.SpringBoot.application.command.product;

import com.SpringBoot.application.command.Command;

import java.util.UUID;

public record DeactivateProductCommand(UUID productId) implements Command {
}
