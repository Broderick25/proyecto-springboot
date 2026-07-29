package com.SpringBoot.domain.product;

import com.SpringBoot.domain.common.AggregateRoot;
import com.SpringBoot.domain.product.events.ProductCreated;
import com.SpringBoot.domain.product.events.ProductDeactivated;
import com.SpringBoot.domain.product.events.ProductUpdated;
import com.SpringBoot.domain.product.events.StockChanged;
import com.SpringBoot.domain.shared.AuditInfo;
import com.SpringBoot.domain.shared.Money;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends AggregateRoot<ProductId> {

    private ProductId id;
    private SKU sku;
    private ProductName name;
    private String description;
    private Money price;
    private Stock stock;
    private CategoryReference category;
    private ProductImage image;
    private boolean active;
    private AuditInfo auditInfo;

    private Product(ProductId id, SKU sku, ProductName name, String description, Money price, Stock stock,
                     CategoryReference category, ProductImage image, AuditInfo auditInfo) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.image = image;
        this.active = true;
        this.auditInfo = auditInfo;
    }

    /**
     * Reconstruye un agregado a partir de estado ya persistido, sin registrar ningún
     * {@link com.SpringBoot.domain.common.DomainEvent} (a diferencia de {@link #create}, que
     * siempre registra {@code ProductCreated}). Uso exclusivo de los mappers de infraestructura
     * al cargar un producto existente.
     */
    public static Product reconstitute(ProductId id, SKU sku, ProductName name, String description, Money price,
                                        Stock stock, CategoryReference category, ProductImage image, boolean active,
                                        AuditInfo auditInfo) {
        Product product = new Product(id, sku, name, description, price, stock, category, image, auditInfo);
        product.active = active;
        return product;
    }

    public static Product create(SKU sku, ProductName name, String description, Money price, Stock stock,
                                  CategoryReference category, ProductImage image, String createdBy) {
        if (sku == null) {
            throw new IllegalArgumentException("sku must not be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        validatePrice(price);
        if (stock == null) {
            throw new IllegalArgumentException("stock must not be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }

        Instant now = Instant.now();
        Product product = new Product(ProductId.generate(), sku, name, description, price, stock, category,
                image, AuditInfo.create(createdBy, now));

        product.registerEvent(new ProductCreated(product.getId(), sku, name, price, now));
        return product;
    }

    public void update(ProductName name, String description, Money price, CategoryReference category,
                        ProductImage image) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        validatePrice(price);
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }

        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.image = image;
        this.auditInfo = this.auditInfo.updateTimestamp();

        registerEvent(new ProductUpdated(this.id, Instant.now()));
    }

    public void incrementStock(int quantity, String reason) {
        validateStockChangeReason(reason);
        Integer oldValue = this.stock.value();
        this.stock = this.stock.increment(quantity);
        this.auditInfo = this.auditInfo.updateTimestamp();
        registerEvent(new StockChanged(this.id, oldValue, this.stock.value(), reason, Instant.now()));
    }

    public void decrementStock(int quantity, String reason) {
        validateStockChangeReason(reason);
        Integer oldValue = this.stock.value();
        this.stock = this.stock.decrement(quantity);
        this.auditInfo = this.auditInfo.updateTimestamp();
        registerEvent(new StockChanged(this.id, oldValue, this.stock.value(), reason, Instant.now()));
    }

    public void changePrice(Money newPrice) {
        validatePrice(newPrice);
        this.price = newPrice;
        this.auditInfo = this.auditInfo.updateTimestamp();
        registerEvent(new ProductUpdated(this.id, Instant.now()));
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("Product is already deactivated");
        }
        this.active = false;
        this.auditInfo = this.auditInfo.updateTimestamp();
        registerEvent(new ProductDeactivated(this.id, Instant.now()));
    }

    public void activate() {
        if (this.active) {
            throw new IllegalStateException("Product is already active");
        }
        this.active = true;
        this.auditInfo = this.auditInfo.updateTimestamp();
        registerEvent(new ProductUpdated(this.id, Instant.now()));
    }

    public boolean hasAvailableStock(int requiredQuantity) {
        return this.stock.hasAvailable(requiredQuantity);
    }

    private static void validateStockChangeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be null or blank");
        }
    }

    private static void validatePrice(Money price) {
        if (price == null) {
            throw new IllegalArgumentException("price must not be null");
        }
        if (price.amount().signum() <= 0) {
            throw new IllegalArgumentException("price must be greater than 0");
        }
    }
}
