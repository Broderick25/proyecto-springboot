# JPA Entity Mapping — Spring Boot 4 / Hibernate 7 / Lombok

Schema: `public` · Base de datos: PostgreSQL · Tablas: `orders`, `order_products`, `products`

---

## Tabla de contenido

1. [Dependencias (pom.xml)](#1-dependencias-pomxml)
2. [Enum `OrderStatus`](#2-enum-orderstatus)
3. [BaseEntity — campos de auditoría](#3-baseentity--campos-de-auditoría)
4. [Entidad `Order`](#4-entidad-order)
5. [Entidad `Product`](#5-entidad-product)
6. [Entidad `OrderProduct`](#6-entidad-orderproduct)
7. [Repositorios](#7-repositorios)
8. [Reglas Lombok + JPA](#8-reglas-lombok--jpa)

---

## 1. Dependencias (pom.xml)

**`erp-domain/pom.xml`** — Lombok y JPA (ya existentes):

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

> `spring-boot-starter-data-jpa` incluye `jakarta.validation-api` de forma transitiva,
> por lo que las anotaciones `@NotNull`, `@Min`, etc. compilan en `erp-domain` sin dependencia extra.

**`erp-api/pom.xml`** — Implementación de Bean Validation (necesaria para ejecutar validaciones en controllers):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Plugin para excluir Lombok del fat-jar (en `erp-api/pom.xml` o `pom.xml` raíz):

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </exclude>
        </excludes>
    </configuration>
</plugin>
```

---

## 2. Enum `OrderStatus`

El SQL define un CHECK constraint con valores fijos. Modelarlo como enum previene valores inválidos desde Java, sin esperar al rechazo del DB.

```java
package com.SpringBoot.domain.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
```

---

## 3. BaseEntity — campos de auditoría

`createdAt` y `updatedAt` son idénticos en `Order` y `Product`. Un `@MappedSuperclass` los centraliza y evita repetición cuando se agreguen más entidades.

```java
package com.SpringBoot.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public abstract class BaseEntity {

    @CreationTimestamp
    @Setter(AccessLevel.NONE)           // Hibernate gestiona este campo; bloquear setter externo
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "timestamp without time zone")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "timestamp without time zone")
    private LocalDateTime updatedAt;
}
```

---

## 4. Entidad `Order`

```java
package com.SpringBoot.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "orders",
    schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_orders_order_number", columnNames = "order_number")
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA requiere constructor sin args; protected evita uso externo
@AllArgsConstructor
@Builder
@ToString(exclude = "orderProducts")                // Excluir colecciones evita StackOverflow en relaciones bidireccionales
@EqualsAndHashCode(onlyExplicitlyIncluded = true)   // equals/hashCode SOLO por PK, nunca por campos lazy
public class Order extends BaseEntity {

    @EqualsAndHashCode.Include
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotBlank
    @Size(max = 200)
    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @NotNull
    @Builder.Default
    @Column(name = "order_date", nullable = false,
            columnDefinition = "timestamp without time zone")
    private LocalDateTime orderDate = LocalDateTime.now();  // @Builder.Default evita null si se omite en el builder

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;       // enum previene valores fuera del CHECK constraint

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @NotBlank
    @Size(min = 3, max = 3)
    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Builder.Default
    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<OrderProduct> orderProducts = new ArrayList<>();

    // ── Helpers de relación bidireccional ──────────────────────────────────────
    // No generados por Lombok: manipulan ambos lados de la asociación

    public void addOrderProduct(OrderProduct item) {
        orderProducts.add(item);
        item.setOrder(this);
    }

    public void removeOrderProduct(OrderProduct item) {
        orderProducts.remove(item);
        item.setOrder(null);
    }
}
```

---

## 5. Entidad `Product`

```java
package com.SpringBoot.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)   // equals/hashCode SOLO por PK, nunca por campos lazy
public class Product extends BaseEntity {

    @EqualsAndHashCode.Include
    @Id
    @UuidGenerator
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

    // Desnormalizado: referencia al ID de documento en MongoDB (erp_catalog_db)
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
```

---

## 6. Entidad `OrderProduct`

```java
package com.SpringBoot.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
    name = "order_products",
    schema = "public",
    indexes = {
        @Index(name = "idx_order_products_order_id",   columnList = "order_id"),
        @Index(name = "idx_order_products_product_id", columnList = "product_id")
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = {"order", "product"})           // Excluir FKs evita StackOverflow en relaciones bidireccionales
@EqualsAndHashCode(onlyExplicitlyIncluded = true)   // equals/hashCode SOLO por PK, nunca por relaciones
public class OrderProduct {

    @EqualsAndHashCode.Include
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    // FK → orders.id | ON DELETE CASCADE
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "order_id",
        nullable = false,
        referencedColumnName = "id",
        foreignKey = @ForeignKey(
            name = "fk_order_products_order",
            foreignKeyDefinition = "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE"
        )
    )
    private Order order;

    // FK → products.id | ON DELETE RESTRICT
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        referencedColumnName = "id",
        foreignKey = @ForeignKey(
            name = "fk_order_products_product",
            foreignKeyDefinition = "FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT"
        )
    )
    private Product product;

    // Snapshot del nombre en el momento del pedido
    @NotBlank
    @Size(max = 200)
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    // ── Lógica de negocio: subtotal se recalcula al cambiar cantidad o precio ──
    // Lombok genera setters estándar; los sobreescribimos solo donde hay lógica extra.

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        recalculateSubtotal();
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        recalculateSubtotal();
    }

    private void recalculateSubtotal() {
        if (this.unitPrice != null && this.quantity != null)
            this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
}
```

---

## 7. Repositorios

> **Nota arquitectónica:** En arquitectura hexagonal estricta, estas interfaces deberían ser puertos de dominio
> sin dependencia de Spring Data, y `erp-infrastructure` proveería la implementación JPA.
> El enfoque actual (extender `JpaRepository` directamente en `erp-domain`) es pragmático y válido
> mientras el proyecto no requiera cambiar la implementación de persistencia.

### OrderRepository

```java
package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(OrderStatus status);   // tipado con enum, no String

    // JOIN FETCH para evitar N+1 al cargar los items de una orden
    @Query("SELECT o FROM Order o JOIN FETCH o.orderProducts WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);
}
```

### ProductRepository

```java
package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);
    List<Product> findByCategoryId(String categoryId);
    List<Product> findByActiveTrue();
    boolean existsBySku(String sku);
}
```

### OrderProductRepository

```java
package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, UUID> {

    List<OrderProduct> findByOrderId(UUID orderId);
    List<OrderProduct> findByProductId(UUID productId);
}
```

---

## 8. Reglas Lombok + JPA

### `@Data` — **nunca usar en entidades JPA**

`@Data` genera `equals/hashCode` basado en todos los campos, lo que rompe con entidades Hibernate porque:
- Las colecciones lazy no están inicializadas al comparar.
- El `hashCode` cambia entre el estado transient y persistido.

**Solución aplicada:** `@Getter` + `@Setter` por separado, y `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` en **todas** las entidades con `@EqualsAndHashCode.Include` solo en el campo `id`.

---

### `@EqualsAndHashCode` — obligatorio en todas las entidades

Sin declararlo explícitamente, Lombok genera `equals/hashCode` con **todos los campos**, incluyendo colecciones lazy. Esto provoca `LazyInitializationException` al comparar entidades fuera de sesión.

**Solución aplicada:** `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` en `Order`, `Product` y `OrderProduct`. Solo el campo `id` está marcado con `@EqualsAndHashCode.Include`.

---

### `@ToString` — excluir relaciones

`@ToString` sobre relaciones `@OneToMany` o `@ManyToOne` dispara la carga lazy y puede causar `StackOverflowError` en relaciones bidireccionales.

**Solución aplicada:** `@ToString(exclude = "orderProducts")` en `Order` y `Product`; `@ToString(exclude = {"order", "product"})` en `OrderProduct`.

---

### `@Builder.Default` — valores por defecto

Cuando se usa `@Builder`, Lombok ignora los valores inicializados en la declaración del campo (`= "PENDING"`, `= new ArrayList<>()`). Sin `@Builder.Default` esos valores se pierden al construir via builder.

**Solución aplicada:** `@Builder.Default` en todos los campos con valor por defecto (`status`, `currency`, `stock`, `active`, `orderDate`, `orderProducts`).

---

### `@NoArgsConstructor(access = AccessLevel.PROTECTED)`

JPA exige un constructor sin argumentos, pero exponerlo como `public` permite crear entidades en estado inválido. `PROTECTED` satisface el requisito de JPA sin abrir el constructor al código de aplicación.

---

### Campos de auditoría — `@MappedSuperclass` + `@Setter(AccessLevel.NONE)`

`createdAt` y `updatedAt` son gestionados exclusivamente por `@CreationTimestamp` / `@UpdateTimestamp` de Hibernate. Se centralizan en `BaseEntity` para evitar repetición. Bloquear el setter evita sobreescrituras accidentales desde la capa de servicio.

---

### `status` como enum — `@Enumerated(EnumType.STRING)`

Un campo `String` permite asignar cualquier valor hasta que el DB lo rechace con el CHECK constraint. Usando `OrderStatus` (enum) el error se produce en compilación.

`EnumType.STRING` persiste el nombre del enum (`"PENDING"`) en vez del ordinal numérico (`0`), lo que hace la columna legible y resistente a reordenamientos del enum.

---

### Bean Validation — detectar errores antes del DB

Las restricciones del SQL (`price >= 0`, `stock >= 0`, `quantity > 0`, `NOT NULL`) se replican con anotaciones `@NotNull`, `@Min`, `@DecimalMin`, `@NotBlank`, `@Size`. Esto permite:
- Rechazar datos inválidos en el controller con `@Valid`, antes de llegar a la capa de persistencia.
- Generar mensajes de error estructurados desde Spring MVC.

Requiere `spring-boot-starter-validation` en `erp-api` para la implementación en runtime.

---

### Setters manuales en `OrderProduct`

Lombok genera setters estándar para todos los campos. En `setQuantity` y `setUnitPrice` se necesita recalcular `subtotal`, por lo que esos dos setters se declaran manualmente — Lombok los omite automáticamente cuando ya existen en la clase.
