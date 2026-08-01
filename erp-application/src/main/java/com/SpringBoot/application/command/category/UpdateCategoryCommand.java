package com.SpringBoot.application.command.category;

import com.SpringBoot.application.command.Command;

public record UpdateCategoryCommand(String id, String code, String value, String description) implements Command {
}
