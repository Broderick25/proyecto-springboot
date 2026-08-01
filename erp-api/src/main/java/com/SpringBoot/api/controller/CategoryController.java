package com.SpringBoot.api.controller;

import com.SpringBoot.application.command.category.CreateCategoryCommand;
import com.SpringBoot.application.command.category.CreateCategoryCommandHandler;
import com.SpringBoot.application.command.category.DeleteCategoryCommand;
import com.SpringBoot.application.command.category.DeleteCategoryCommandHandler;
import com.SpringBoot.application.command.category.UpdateCategoryCommand;
import com.SpringBoot.application.command.category.UpdateCategoryCommandHandler;
import com.SpringBoot.application.query.category.GetCategoryByCodeQuery;
import com.SpringBoot.application.query.category.GetCategoryByCodeQueryHandler;
import com.SpringBoot.application.query.category.GetCategoryByIdQuery;
import com.SpringBoot.application.query.category.GetCategoryByIdQueryHandler;
import com.SpringBoot.application.query.category.ListCategoriesQuery;
import com.SpringBoot.application.query.category.ListCategoriesQueryHandler;
import com.SpringBoot.application.query.category.view.CatalogView;
import com.SpringBoot.application.query.category.view.ItemView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CreateCategoryCommandHandler createCategoryCommandHandler;
    private final UpdateCategoryCommandHandler updateCategoryCommandHandler;
    private final DeleteCategoryCommandHandler deleteCategoryCommandHandler;
    private final ListCategoriesQueryHandler listCategoriesQueryHandler;
    private final GetCategoryByCodeQueryHandler getCategoryByCodeQueryHandler;
    private final GetCategoryByIdQueryHandler getCategoryByIdQueryHandler;

    public CategoryController(CreateCategoryCommandHandler createCategoryCommandHandler,
                               UpdateCategoryCommandHandler updateCategoryCommandHandler,
                               DeleteCategoryCommandHandler deleteCategoryCommandHandler,
                               ListCategoriesQueryHandler listCategoriesQueryHandler,
                               GetCategoryByCodeQueryHandler getCategoryByCodeQueryHandler,
                               GetCategoryByIdQueryHandler getCategoryByIdQueryHandler) {
        this.createCategoryCommandHandler = createCategoryCommandHandler;
        this.updateCategoryCommandHandler = updateCategoryCommandHandler;
        this.deleteCategoryCommandHandler = deleteCategoryCommandHandler;
        this.listCategoriesQueryHandler = listCategoriesQueryHandler;
        this.getCategoryByCodeQueryHandler = getCategoryByCodeQueryHandler;
        this.getCategoryByIdQueryHandler = getCategoryByIdQueryHandler;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody CreateCategoryCommand command) {
        String categoryId = createCategoryCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody UpdateCategoryRequest request) {
        updateCategoryCommandHandler.handle(
                new UpdateCategoryCommand(id, request.code(), request.value(), request.description()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteCategoryCommandHandler.handle(new DeleteCategoryCommand(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CatalogView> list() {
        return ResponseEntity.ok(listCategoriesQueryHandler.handle(new ListCategoriesQuery()));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ItemView> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(getCategoryByCodeQueryHandler.handle(new GetCategoryByCodeQuery(code)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemView> getById(@PathVariable String id) {
        return ResponseEntity.ok(getCategoryByIdQueryHandler.handle(new GetCategoryByIdQuery(id)));
    }

    public record UpdateCategoryRequest(String code, String value, String description) {
    }
}
