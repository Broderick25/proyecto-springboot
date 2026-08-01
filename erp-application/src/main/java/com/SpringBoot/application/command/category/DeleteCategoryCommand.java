package com.SpringBoot.application.command.category;

import com.SpringBoot.application.command.Command;

public record DeleteCategoryCommand(String id) implements Command {
}
