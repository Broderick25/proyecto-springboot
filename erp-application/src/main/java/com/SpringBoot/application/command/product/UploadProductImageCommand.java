package com.SpringBoot.application.command.product;

import com.SpringBoot.application.command.Command;

import java.util.UUID;

public record UploadProductImageCommand(UUID productId, byte[] content, String contentType) implements Command {
}
