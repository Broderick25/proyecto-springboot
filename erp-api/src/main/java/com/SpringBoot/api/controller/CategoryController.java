package com.SpringBoot.api.controller;

import com.SpringBoot.application.command.category.CreateCategoryCommand;
import com.SpringBoot.application.command.category.CreateCategoryCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CreateCategoryCommandHandler createCategoryCommandHandler;

    public CategoryController(CreateCategoryCommandHandler createCategoryCommandHandler) {
        this.createCategoryCommandHandler = createCategoryCommandHandler;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody CreateCategoryCommand command) {
        String categoryId = createCategoryCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryId);
    }
}
