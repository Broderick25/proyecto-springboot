package com.SpringBoot.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador placeholder para el API
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public String health() {
        return "ERP Lite API is running!";
    }
}
