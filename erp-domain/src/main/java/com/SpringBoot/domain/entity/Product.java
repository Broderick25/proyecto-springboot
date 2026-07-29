package com.SpringBoot.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "products",
    schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_products_sku", columnNames = "sku")
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = "orderProducts")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product extends BaseEntity {

    // Sin generador: el id siempre lo asigna la app (el agregado DDD lo genera en Product.create(),
    // y ese mismo id se usa en los eventos de dominio ya publicados — @UuidGenerator lo
    // sobreescribiría siempre en el insert, sin importar el valor ya asignado, rompiendo esa
    // consistencia).
    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Min(0)
    @Builder.Default
    @Column(name = "stock", nullable = false, columnDefinition = "integer default 0")
    private Integer stock = 0;

    // Referencia al ID de documento en MongoDB (erp_catalog_db)
    @Size(max = 100)
    @Column(name = "category_id", length = 100)
    private String categoryId;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    private Boolean active = Boolean.TRUE;

    // ON DELETE RESTRICT → NO se propaga cascade de borrado desde Product
    @Builder.Default
    @OneToMany(
        mappedBy = "product",
        cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        fetch = FetchType.LAZY
    )
    private List<OrderProduct> orderProducts = new ArrayList<>();
}
