package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.Command;

import java.util.UUID;

public record ShipOrderCommand(UUID orderId) implements Command {
}
