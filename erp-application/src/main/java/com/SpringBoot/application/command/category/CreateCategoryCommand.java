package com.SpringBoot.application.command.category;

import com.SpringBoot.application.command.Command;

public record CreateCategoryCommand(String id, String code, String name, String description) implements Command {
}
