package com.SpringBoot.api.controller;

import com.SpringBoot.application.command.product.ActivateProductCommand;
import com.SpringBoot.application.command.product.ActivateProductCommandHandler;
import com.SpringBoot.application.command.product.ChangeProductPriceCommand;
import com.SpringBoot.application.command.product.ChangeProductPriceCommandHandler;
import com.SpringBoot.application.command.product.CreateProductCommand;
import com.SpringBoot.application.command.product.CreateProductCommandHandler;
import com.SpringBoot.application.command.product.DeactivateProductCommand;
import com.SpringBoot.application.command.product.DeactivateProductCommandHandler;
import com.SpringBoot.application.command.product.DecrementProductStockCommand;
import com.SpringBoot.application.command.product.DecrementProductStockCommandHandler;
import com.SpringBoot.application.command.product.IncrementProductStockCommand;
import com.SpringBoot.application.command.product.IncrementProductStockCommandHandler;
import com.SpringBoot.application.command.product.UpdateProductCommand;
import com.SpringBoot.application.command.product.UpdateProductCommandHandler;
import com.SpringBoot.application.query.product.GetProductByIdQuery;
import com.SpringBoot.application.query.product.GetProductByIdQueryHandler;
import com.SpringBoot.application.query.product.ListProductsQuery;
import com.SpringBoot.application.query.product.ListProductsQueryHandler;
import com.SpringBoot.application.query.product.view.ProductView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final CreateProductCommandHandler createProductCommandHandler;
    private final UpdateProductCommandHandler updateProductCommandHandler;
    private final IncrementProductStockCommandHandler incrementProductStockCommandHandler;
    private final DecrementProductStockCommandHandler decrementProductStockCommandHandler;
    private final ChangeProductPriceCommandHandler changeProductPriceCommandHandler;
    private final ActivateProductCommandHandler activateProductCommandHandler;
    private final DeactivateProductCommandHandler deactivateProductCommandHandler;
    private final GetProductByIdQueryHandler getProductByIdQueryHandler;
    private final ListProductsQueryHandler listProductsQueryHandler;

    public ProductController(CreateProductCommandHandler createProductCommandHandler,
                              UpdateProductCommandHandler updateProductCommandHandler,
                              IncrementProductStockCommandHandler incrementProductStockCommandHandler,
                              DecrementProductStockCommandHandler decrementProductStockCommandHandler,
                              ChangeProductPriceCommandHandler changeProductPriceCommandHandler,
                              ActivateProductCommandHandler activateProductCommandHandler,
                              DeactivateProductCommandHandler deactivateProductCommandHandler,
                              GetProductByIdQueryHandler getProductByIdQueryHandler,
                              ListProductsQueryHandler listProductsQueryHandler) {
        this.createProductCommandHandler = createProductCommandHandler;
        this.updateProductCommandHandler = updateProductCommandHandler;
        this.incrementProductStockCommandHandler = incrementProductStockCommandHandler;
        this.decrementProductStockCommandHandler = decrementProductStockCommandHandler;
        this.changeProductPriceCommandHandler = changeProductPriceCommandHandler;
        this.activateProductCommandHandler = activateProductCommandHandler;
        this.deactivateProductCommandHandler = deactivateProductCommandHandler;
        this.getProductByIdQueryHandler = getProductByIdQueryHandler;
        this.listProductsQueryHandler = listProductsQueryHandler;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductView> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getProductByIdQueryHandler.handle(new GetProductByIdQuery(id)));
    }

    @GetMapping
    public ResponseEntity<List<ProductView>> list(
            @RequestParam(name = "onlyActive", defaultValue = "false") boolean onlyActive) {
        return ResponseEntity.ok(listProductsQueryHandler.handle(new ListProductsQuery(onlyActive)));
    }

    @PostMapping
    public ResponseEntity<UUID> create(@RequestBody CreateProductCommand command) {
        UUID productId = createProductCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody UpdateProductRequest request) {
        updateProductCommandHandler.handle(new UpdateProductCommand(id, request.name(), request.description(),
                request.price(), request.categoryId(), request.imageUrl()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/stock/increment")
    public ResponseEntity<Void> incrementStock(@PathVariable UUID id, @RequestBody StockAdjustmentRequest request) {
        incrementProductStockCommandHandler.handle(
                new IncrementProductStockCommand(id, request.quantity(), request.reason()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/stock/decrement")
    public ResponseEntity<Void> decrementStock(@PathVariable UUID id, @RequestBody StockAdjustmentRequest request) {
        decrementProductStockCommandHandler.handle(
                new DecrementProductStockCommand(id, request.quantity(), request.reason()));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<Void> changePrice(@PathVariable UUID id, @RequestBody ChangePriceRequest request) {
        changeProductPriceCommandHandler.handle(new ChangeProductPriceCommand(id, request.newPrice()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        activateProductCommandHandler.handle(new ActivateProductCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateProductCommandHandler.handle(new DeactivateProductCommand(id));
        return ResponseEntity.ok().build();
    }

    public record UpdateProductRequest(String name, String description, BigDecimal price, String categoryId,
                                        String imageUrl) {
    }

    public record StockAdjustmentRequest(int quantity, String reason) {
    }

    public record ChangePriceRequest(BigDecimal newPrice) {
    }
}
