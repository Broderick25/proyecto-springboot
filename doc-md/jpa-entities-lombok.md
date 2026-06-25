# JPA Entity Mapping — Spring Boot 4 / Hibernate 7 / Lombok

Schema: `public` · Base de datos: PostgreSQL · Tablas: `orders`, `order_products`, `products`

---

## Tabla de contenido

1. [Dependencia Lombok (pom.xml)](#1-dependencia-lombok-pomxml)
2. [Entidad `Order`](#2-entidad-order)
3. [Entidad `Product`](#3-entidad-product)
4. [Entidad `OrderProduct`](#4-entidad-orderproduct)
5. [Repositorios](#5-repositorios)
6. [Reglas Lombok + JPA](#6-reglas-lombok--jpa)

---

## 1. Dependencia Lombok (pom.xml)

Agregar al `pom.xml` existente:

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

Y excluirlo del fat-jar en el plugin:

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

## 2. Entidad `Order`

```java
package com.example.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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
public class Order {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "order_date", nullable = false,
            columnDefinition = "timestamp without time zone")
    private LocalDateTime orderDate;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @CreationTimestamp
    @Setter(AccessLevel.NONE)                       // Hibernate gestiona este campo; bloquear setter externo
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "timestamp without time zone")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "timestamp without time zone")
    private LocalDateTime updatedAt;

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

## 3. Entidad `Product`

```java
package com.example.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class Product {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(name = "stock", nullable = false, columnDefinition = "integer default 0")
    private Integer stock = 0;

    // Desnormalizado: no existe tabla categories en el schema
    @Column(name = "category_id", length = 100)
    private String categoryId;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    private Boolean active = Boolean.TRUE;

    @CreationTimestamp
    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "timestamp without time zone")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "timestamp without time zone")
    private LocalDateTime updatedAt;

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

## 4. Entidad `OrderProduct`

```java
package com.example.domain.entity;

import jakarta.persistence.*;
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
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

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

## 5. Repositorios

### OrderRepository

```java
package com.example.domain.repository;

import com.example.domain.entity.Order;
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
    List<Order> findByStatus(String status);

    // JOIN FETCH para evitar N+1 al cargar los items de una orden
    @Query("SELECT o FROM Order o JOIN FETCH o.orderProducts WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);
}
```

### ProductRepository

```java
package com.example.domain.repository;

import com.example.domain.entity.Product;
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
package com.example.domain.repository;

import com.example.domain.entity.OrderProduct;
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

## 6. Reglas Lombok + JPA

Estas son las combinaciones problemáticas que este mapeo resuelve explícitamente.

### `@Data` — **nunca usar en entidades JPA**

`@Data` genera `equals/hashCode` basado en todos los campos, lo que rompe con entidades Hibernate porque:
- Las colecciones lazy no están inicializadas al comparar.
- El `hashCode` cambia entre el estado transient y persistido.

**Solución aplicada:** `@Getter` + `@Setter` por separado, y `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` en `OrderProduct`.

### `@ToString` — excluir relaciones

`@ToString` sobre relaciones `@OneToMany` o `@ManyToOne` dispara la carga lazy y puede causar `StackOverflowError` en relaciones bidireccionales.

**Solución aplicada:** `@ToString(exclude = "orderProducts")` en `Order` y `Product`; `@ToString(exclude = {"order", "product"})` en `OrderProduct`.

### `@Builder.Default` — valores por defecto

Cuando se usa `@Builder`, Lombok ignora los valores inicializados en la declaración del campo (`= "PENDING"`, `= new ArrayList<>()`). Sin `@Builder.Default` esos valores se pierden al construir via builder.

**Solución aplicada:** `@Builder.Default` en todos los campos con valor por defecto (`status`, `currency`, `stock`, `active`, `orderProducts`).

### `@NoArgsConstructor(access = AccessLevel.PROTECTED)`

JPA exige un constructor sin argumentos, pero exponerlo como `public` permite crear entidades en estado inválido. `PROTECTED` satisface el requisito de JPA sin abrir el constructor al código de aplicación.

### Campos de auditoría — `@Setter(AccessLevel.NONE)`

`createdAt` y `updatedAt` son gestionados exclusivamente por `@CreationTimestamp` / `@UpdateTimestamp` de Hibernate. Bloquear el setter evita sobreescrituras accidentales desde la capa de servicio.

### Setters manuales en `OrderProduct`

Lombok genera setters estándar para todos los campos. En `setQuantity` y `setUnitPrice` se necesita recalcular `subtotal`, por lo que esos dos setters se declaran manualmente — Lombok los omite automáticamente cuando ya existen en la clase.
