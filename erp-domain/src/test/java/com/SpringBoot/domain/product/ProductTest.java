package com.SpringBoot.domain.product;

import com.SpringBoot.domain.product.events.ProductCreated;
import com.SpringBoot.domain.product.events.ProductDeactivated;
import com.SpringBoot.domain.product.events.StockChanged;
import com.SpringBoot.domain.shared.Money;

import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private final Currency USD = Currency.getInstance("USD");

    @Test
    void create_generaProductoActivoConEventoProductCreated() {
        Product product = Product.create(
                SKU.of("SKU-001"),
                ProductName.of("Laptop"),
                "Descripción",
                Money.of(1200, USD),
                Stock.of(10),
                CategoryReference.of("cat-electronics"),
                ProductImage.of("http://img/laptop.png"),
                "admin"
        );

        assertThat(product.getId()).isNotNull();
        assertThat(product.isActive()).isTrue();
        assertThat(product.getAuditInfo()).isNotNull();
        assertThat(product.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(ProductCreated.class);
    }

    @Test
    void incrementStock_aumentaStockYRegistraStockChanged() {
        Product product = crearProductoBase();
        product.clearDomainEvents();

        product.incrementStock(5, "reposición");

        assertThat(product.getStock().value()).isEqualTo(15);
        assertThat(product.getDomainEvents())
                .filteredOn(e -> e instanceof StockChanged)
                .hasSize(1);
    }

    @Test
    void decrementStock_disminuyeStockYRegistraStockChanged() {
        Product product = crearProductoBase();
        product.clearDomainEvents();

        product.decrementStock(4, "venta");

        assertThat(product.getStock().value()).isEqualTo(6);
        assertThat(product.getDomainEvents())
                .filteredOn(e -> e instanceof StockChanged)
                .hasSize(1);
    }

    @Test
    void decrementStock_noPermiteDejarStockNegativo() {
        Product product = crearProductoBase(); // stock = 10

        assertThatThrownBy(() -> product.decrementStock(20, "venta"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changePrice_rechazaMontoCeroOMenor() {
        Product product = crearProductoBase();

        assertThatThrownBy(() -> product.changePrice(Money.of(0, USD)))
                .isInstanceOf(IllegalArgumentException.class);
        // Nota: Money permite 0 como válido; la regla "precio > 0" es
        // una invariante propia de Product, no de Money — por eso este test
        // vive aquí y no en MoneyTest.
    }

    @Test
    void changePrice_aceptaMontoPositivoYActualiza() {
        Product product = crearProductoBase();

        product.changePrice(Money.of(999, USD));

        assertThat(product.getPrice().amount()).isEqualByComparingTo("999");
    }

    @Test
    void deactivate_marcaInactivoYRegistraEvento() {
        Product product = crearProductoBase();
        product.clearDomainEvents();

        product.deactivate();

        assertThat(product.isActive()).isFalse();
        assertThat(product.getDomainEvents())
                .filteredOn(e -> e instanceof ProductDeactivated)
                .hasSize(1);
    }

    @Test
    void activate_marcaActivoNuevamente() {
        Product product = crearProductoBase();
        product.deactivate();

        product.activate();

        assertThat(product.isActive()).isTrue();
    }

    @Test
    void hasAvailableStock_reflejaStockActual() {
        Product product = crearProductoBase(); // stock = 10

        assertThat(product.hasAvailableStock(5)).isTrue();
        assertThat(product.hasAvailableStock(50)).isFalse();
    }

    private Product crearProductoBase() {
        return Product.create(
                SKU.of("SKU-001"),
                ProductName.of("Laptop"),
                "Descripción",
                Money.of(1200, USD),
                Stock.of(10),
                CategoryReference.of("cat-electronics"),
                ProductImage.of("http://img/laptop.png"),
                "admin"
        );
    }
}
