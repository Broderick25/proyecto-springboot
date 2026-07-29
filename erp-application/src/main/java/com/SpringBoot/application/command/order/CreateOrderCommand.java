package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.Command;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(Long customerId, List<OrderLine> items, String createdBy) implements Command {

    public record OrderLine(UUID productId, int quantity) {
    }
}
