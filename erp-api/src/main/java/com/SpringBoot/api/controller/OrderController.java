package com.SpringBoot.api.controller;

import com.SpringBoot.application.usecase.SendOrderConfirmationEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final SendOrderConfirmationEmailService sendOrderConfirmationEmailService;

    public OrderController(SendOrderConfirmationEmailService sendOrderConfirmationEmailService) {
        this.sendOrderConfirmationEmailService = sendOrderConfirmationEmailService;
    }

    @PostMapping("/{id}/send-confirmation")
    public ResponseEntity<Void> sendConfirmation(@PathVariable UUID id) {
        sendOrderConfirmationEmailService.sendConfirmation(id);
        return ResponseEntity.ok().build();
    }
}
