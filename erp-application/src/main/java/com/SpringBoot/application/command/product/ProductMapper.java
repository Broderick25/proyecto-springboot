package com.SpringBoot.application.command.product;

import com.SpringBoot.domain.product.CategoryReference;
import com.SpringBoot.domain.product.ProductId;
import com.SpringBoot.domain.product.ProductImage;
import com.SpringBoot.domain.product.ProductName;
import com.SpringBoot.domain.product.SKU;
import com.SpringBoot.domain.product.Stock;
import com.SpringBoot.domain.shared.AuditInfo;
import com.SpringBoot.domain.shared.Money;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.Currency;

/**
 * Mapea entre la entidad JPA ({@code domain.entity.Product}, lo que realmente se persiste en
 * Postgres) y el agregado DDD ({@code domain.product.Product}, donde vive la lógica de negocio).
 *
 * <p>Dos diferencias de forma entre ambos modelos, resueltas aquí de forma pragmática:
 * <ul>
 *   <li>La entidad JPA no tiene columna {@code currency}; se asume USD fijo.</li>
 *   <li>La entidad JPA no tiene columna {@code created_by}; se usa "system" como relleno,
 *       ya que {@code AuditInfo.createdBy} no participa en ninguna regla de negocio.</li>
 *   <li>{@code imageUrl} en la entidad JPA guarda una key de S3 (no una URL absoluta), a
 *       diferencia de {@link ProductImage}, que exige URL absoluta; si no es una URL válida
 *       se mapea a {@code null} en el agregado. Por eso {@link #copyToEntity} nunca escribe
 *       {@code null} en {@code imageUrl} cuando el agregado no trae imagen — dejaría el campo
 *       tal como estaba en vez de borrar la key existente en cada comando que no la toca.</li>
 * </ul>
 */
public final class ProductMapper {

    private static final Currency CURRENCY = Currency.getInstance("USD");
    private static final String DEFAULT_CREATED_BY = "system";

    private ProductMapper() {
    }

    public static com.SpringBoot.domain.product.Product toAggregate(com.SpringBoot.domain.entity.Product entity) {
        return com.SpringBoot.domain.product.Product.reconstitute(
                ProductId.of(entity.getId()),
                SKU.of(entity.getSku()),
                ProductName.of(entity.getName()),
                entity.getDescription(),
                Money.of(entity.getPrice(), CURRENCY),
                Stock.of(entity.getStock()),
                CategoryReference.of(entity.getCategoryId()),
                toProductImage(entity.getImageUrl()),
                Boolean.TRUE.equals(entity.getActive()),
                AuditInfo.create(DEFAULT_CREATED_BY, entity.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
    }

    public static com.SpringBoot.domain.entity.Product toNewEntity(com.SpringBoot.domain.product.Product aggregate) {
        com.SpringBoot.domain.entity.Product entity = com.SpringBoot.domain.entity.Product.builder()
                .id(aggregate.getId().value())
                .build();
        copyToEntity(aggregate, entity);
        return entity;
    }

    public static void copyToEntity(com.SpringBoot.domain.product.Product aggregate,
                                     com.SpringBoot.domain.entity.Product entity) {
        entity.setSku(aggregate.getSku().value());
        entity.setName(aggregate.getName().value());
        entity.setDescription(aggregate.getDescription());
        entity.setPrice(aggregate.getPrice().amount());
        entity.setStock(aggregate.getStock().value());
        entity.setCategoryId(aggregate.getCategory().categoryId());
        if (aggregate.getImage() != null) {
            entity.setImageUrl(aggregate.getImage().getFullUrl());
        }
        entity.setActive(aggregate.isActive());
    }

    private static ProductImage toProductImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || !isAbsoluteUrl(imageUrl)) {
            return null;
        }
        return ProductImage.of(imageUrl);
    }

    private static boolean isAbsoluteUrl(String value) {
        try {
            URI uri = new URI(value);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
