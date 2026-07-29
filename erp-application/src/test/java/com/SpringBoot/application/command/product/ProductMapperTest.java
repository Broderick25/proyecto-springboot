package com.SpringBoot.application.command.product;

import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.product.CategoryReference;
import com.SpringBoot.domain.product.ProductName;
import com.SpringBoot.domain.product.SKU;
import com.SpringBoot.domain.product.Stock;
import com.SpringBoot.domain.shared.Money;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    @Test
    void toAggregate_mapeaLosCamposBasicosDesdeLaEntidad() {
        Product entity = productEntity(UUID.randomUUID());

        com.SpringBoot.domain.product.Product aggregate = ProductMapper.toAggregate(entity);

        assertThat(aggregate.getId().value()).isEqualTo(entity.getId());
        assertThat(aggregate.getSku().value()).isEqualTo(entity.getSku());
        assertThat(aggregate.getName().value()).isEqualTo(entity.getName());
        assertThat(aggregate.getDescription()).isEqualTo(entity.getDescription());
        assertThat(aggregate.getPrice().amount()).isEqualByComparingTo(entity.getPrice());
        assertThat(aggregate.getStock().value()).isEqualTo(entity.getStock());
        assertThat(aggregate.getCategory().categoryId()).isEqualTo(entity.getCategoryId());
        assertThat(aggregate.isActive()).isEqualTo(entity.getActive());
    }

    @Test
    void toAggregate_mapeaImagenNula_cuandoImageUrlNoEsUnaUrlAbsoluta() {
        Product entity = productEntity(UUID.randomUUID());
        entity.setImageUrl("products/" + entity.getId() + "/foto.png");

        com.SpringBoot.domain.product.Product aggregate = ProductMapper.toAggregate(entity);

        assertThat(aggregate.getImage()).isNull();
    }

    @Test
    void toAggregate_mapeaImagen_cuandoImageUrlEsUnaUrlAbsoluta() {
        Product entity = productEntity(UUID.randomUUID());
        entity.setImageUrl("https://cdn.example.com/products/foto.png");

        com.SpringBoot.domain.product.Product aggregate = ProductMapper.toAggregate(entity);

        assertThat(aggregate.getImage()).isNotNull();
        assertThat(aggregate.getImage().getFullUrl()).isEqualTo("https://cdn.example.com/products/foto.png");
    }

    @Test
    void copyToEntity_reflejaEnLaEntidadLosCambiosDelAgregado() {
        Product entity = productEntity(UUID.randomUUID());
        com.SpringBoot.domain.product.Product aggregate = ProductMapper.toAggregate(entity);
        aggregate.incrementStock(5, "reposición");
        aggregate.deactivate();

        ProductMapper.copyToEntity(aggregate, entity);

        assertThat(entity.getStock()).isEqualTo(15);
        assertThat(entity.getActive()).isFalse();
    }

    @Test
    void toNewEntity_usaElIdGeneradoPorElAgregado() {
        com.SpringBoot.domain.product.Product aggregate = com.SpringBoot.domain.product.Product.create(
                SKU.of("LAPTOP-001"),
                ProductName.of("Laptop Dell XPS 15"),
                "Laptop de alto rendimiento",
                Money.of(new BigDecimal("1499.99"), Currency.getInstance("USD")),
                Stock.of(10),
                CategoryReference.of("cat-electronics"),
                null,
                "admin");

        Product entity = ProductMapper.toNewEntity(aggregate);

        assertThat(entity.getId()).isEqualTo(aggregate.getId().value());
        assertThat(entity.getSku()).isEqualTo("LAPTOP-001");
        assertThat(entity.getStock()).isEqualTo(10);
        assertThat(entity.getActive()).isTrue();
    }

    static Product productEntity(UUID id) {
        Product entity = Product.builder()
                .id(id)
                .sku("LAPTOP-001")
                .name("Laptop Dell XPS 15")
                .description("Laptop de alto rendimiento")
                .price(new BigDecimal("1499.99"))
                .stock(10)
                .categoryId("cat-electronics")
                .active(true)
                .build();
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        return entity;
    }
}
