package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.Command;

import java.util.UUID;

public record CancelOrderCommand(UUID orderId, String reason) implements Command {
}
