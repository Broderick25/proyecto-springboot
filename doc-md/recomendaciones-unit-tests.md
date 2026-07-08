# Recomendaciones de Unit Tests — ERP Lite

> Estado actual: **0% de cobertura real**. Lo único existente es el `contextLoads()`
> por defecto de Spring Boot (`ErpApiApplicationTests`). Este documento propone un plan
> priorizado con esqueletos de código listos para completar.

---

## Tabla de contenido

1. [Prioridad y orden sugerido](#1-prioridad-y-orden-sugerido)
2. [Value Objects (`shared`)](#2-value-objects-shared)
3. [Aggregate `Product`](#3-aggregate-product)
4. [Aggregate `Order`](#4-aggregate-order)
5. [Building blocks base (`common`)](#5-building-blocks-base-common)
6. [Repositorios JPA (Testcontainers)](#6-repositorios-jpa-testcontainers)
7. [Repositorios MongoDB (Testcontainers)](#7-repositorios-mongodb-testcontainers)
8. [Qué NO testear todavía](#8-qué-no-testear-todavía)
9. [Dependencias necesarias](#9-dependencias-necesarias)

---

## 1. Prioridad y orden sugerido

| Orden | Paquete | Por qué primero |
|---|---|---|
| 1 | `shared/` (Value Objects) | Sin dependencias, records puros, base de todo lo demás |
| 2 | `product/` | Usa solo VOs ya testeados |
| 3 | `order/` | El más crítico: máquina de estados + cálculo de totales |
| 4 | `common/` | Building blocks abstractos, pocos casos |
| 5 | `repository/` (JPA) | Requiere Testcontainers Postgres |
| 6 | `document/` (Mongo) | Requiere Testcontainers Mongo |

---

## 2. Value Objects (`shared`)

### `MoneyTest.java`

```java
package com.SpringBoot.domain.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private final Currency USD = Currency.getInstance("USD");
    private final Currency EUR = Currency.getInstance("EUR");

    @Test
    void of_creaMoneyValidoConMontoPositivo() {
        Money money = Money.of(1299.99, USD);

        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.valueOf(1299.99));
        assertThat(money.currency()).isEqualTo(USD);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -100.0})
    void constructor_rechazaMontoNegativo(double montoInvalido) {
        assertThatThrownBy(() -> Money.of(montoInvalido, USD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void add_sumaCorrectamenteConMismaMoneda() {
        Money a = Money.of(100, USD);
        Money b = Money.of(50, USD);

        Money result = a.add(b);

        assertThat(result.amount()).isEqualByComparingTo("150");
    }

    @Test
    void add_lanzaExcepcionConMonedasDistintas() {
        Money usd = Money.of(100, USD);
        Money eur = Money.of(50, EUR);

        assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void subtract_noPermiteResultadoNegativo() {
        Money a = Money.of(50, USD);
        Money b = Money.of(100, USD);

        assertThatThrownBy(() -> a.subtract(b))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void multiply_porQuantityNoMutaElOriginal() {
        Money price = Money.of(1299.99, USD);

        Money total = price.multiply(Quantity.of(3));

        assertThat(price.amount()).isEqualByComparingTo("1299.99"); // no mutado
        assertThat(total.amount()).isEqualByComparingTo("3899.97");
    }
}
```

### `QuantityTest.java`

```java
package com.SpringBoot.domain.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuantityTest {

    @Test
    void of_creaQuantityValidaConValorPositivo() {
        Quantity q = Quantity.of(5);
        assertThat(q.value()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void of_rechazaValorNoPositivo(int valorInvalido) {
        assertThatThrownBy(() -> Quantity.of(valorInvalido))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

### `StockTest.java`

```java
package com.SpringBoot.domain.product;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    @Test
    void increment_devuelveNuevaInstanciaInmutable() {
        Stock original = Stock.of(10);

        Stock incremented = original.increment(5);

        assertThat(original.value()).isEqualTo(10);       // no mutado
        assertThat(incremented.value()).isEqualTo(15);
    }

    @Test
    void decrement_devuelveNuevaInstanciaInmutable() {
        Stock original = Stock.of(10);

        Stock decremented = original.decrement(3);

        assertThat(original.value()).isEqualTo(10);
        assertThat(decremented.value()).isEqualTo(7);
    }

    @Test
    void decrement_noPermiteResultadoNegativo() {
        Stock stock = Stock.of(5);

        assertThatThrownBy(() -> stock.decrement(10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hasAvailable_retornaTrueSiHayStockSuficiente() {
        Stock stock = Stock.of(10);

        assertThat(stock.hasAvailable(5)).isTrue();
        assertThat(stock.hasAvailable(15)).isFalse();
    }
}
```

### `CustomerIdTest.java` / `EmailTest.java` / `AuditInfoTest.java` (esqueleto compacto)

```java
package com.SpringBoot.domain.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerIdTest {

    @Test
    void of_aceptaValorPositivo() {
        assertThat(CustomerId.of(1L).value()).isEqualTo(1L);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void of_rechazaValorNoPositivo(long valor) {
        assertThatThrownBy(() -> CustomerId.of(valor))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {"user@example.com", "a.b+c@sub.example.co"})
    void of_aceptaEmailsValidos(String email) {
        assertThat(Email.of(email).value()).isEqualTo(email);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalido", "sin-arroba.com", "@sin-usuario.com", "user@"})
    void of_rechazaEmailsInvalidos(String email) {
        assertThatThrownBy(() -> Email.of(email))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

class AuditInfoTest {

    @Test
    void create_rechazaCreatedByBlank() {
        assertThatThrownBy(() -> AuditInfo.create("  ", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateTimestamp_devuelveCopiaConUpdatedAtRefrescado() {
        Instant t0 = Instant.now();
        AuditInfo original = AuditInfo.create("admin", t0);

        AuditInfo updated = original.updateTimestamp();

        assertThat(original.updatedAt()).isEqualTo(original.createdAt()); // no mutado
        assertThat(updated.updatedAt()).isAfterOrEqualTo(t0);
    }
}
```

> Ajusta los nombres de métodos factory (`of`, `create`) y accessors (`value()`, `amount()`)
> a la firma real de tus records si difieren.

---

## 3. Aggregate `Product`

### `ProductTest.java`

```java
package com.SpringBoot.domain.product;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
                "admin" // Product.create() recibe createdBy como último parámetro
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
```

> **Ajuste verificado contra el código real:** `Product.create(...)` en
> `product/Product.java` recibe un parámetro adicional `String createdBy` al
> final de la firma (no está en la versión original de este documento). Todas
> las llamadas anteriores ya lo incluyen.

---

## 4. Aggregate `Order`

Este es el **más importante**: contiene la máquina de estados y comportamiento documentado
como "no validado actualmente" que conviene fijar con tests antes de refactorizar.

### `OrderStatusTest.java`

```java
package com.SpringBoot.domain.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @ParameterizedTest
    @CsvSource({
            "PENDING, CONFIRMED, true",
            "PENDING, CANCELLED, true",
            "CONFIRMED, SHIPPED, true",
            "CONFIRMED, CANCELLED, true",
            "SHIPPED, DELIVERED, true",
            "DELIVERED, CANCELLED, false",   // transición inválida explícita
            "CANCELLED, CONFIRMED, false",
            "PENDING, SHIPPED, false",       // no puede saltar CONFIRMED
            "PENDING, DELIVERED, false"
    })
    void canTransitionTo_respetaLaMaquinaDeEstados(String origen, String destino, boolean esperado) {
        OrderStatus from = new OrderStatus(origen);
        OrderStatus to = new OrderStatus(destino);

        assertThat(from.canTransitionTo(to)).isEqualTo(esperado);
    }
}
```

### `OrderTest.java`

> **Ajustado contra el código real** (`order/Order.java`, `order/Customer.java`,
> `order/OrderNumber.java`, `order/OrderItem.java`): `Order.create(...)` no recibe
> `CustomerId`/`String` sueltos sino un value object `Customer` y un `OrderNumber`
> ya validado, más el `createdBy` al final. `OrderItem` no es un record: expone
> getters de Lombok (`getUnitPrice()`, `getProductName()`, `getSubtotal()`), no
> `price()`/`productName()`.

```java
package com.SpringBoot.domain.order;

import com.SpringBoot.domain.product.CategoryReference;
import com.SpringBoot.domain.product.Product;
import com.SpringBoot.domain.product.ProductImage;
import com.SpringBoot.domain.product.ProductName;
import com.SpringBoot.domain.product.SKU;
import com.SpringBoot.domain.product.Stock;
import com.SpringBoot.domain.shared.CustomerId;
import com.SpringBoot.domain.shared.Money;
import com.SpringBoot.domain.shared.Quantity;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private final Currency USD = Currency.getInstance("USD");

    @Test
    void create_rechazaListaDeItemsVacia() {
        Customer customer = Customer.of(CustomerId.of(1L), "Cliente Demo");

        assertThatThrownBy(() ->
                Order.create(OrderNumber.of("ORD-2026-001"), customer, List.of(), "admin")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_calculaTotalAmountSumandoSubtotales() {
        OrderItem item1 = OrderItem.from(productoDemo("MOU-001", "Mouse", 20, USD), Quantity.of(2));   // 40
        OrderItem item2 = OrderItem.from(productoDemo("KEY-001", "Teclado", 60, USD), Quantity.of(1)); // 60
        Customer customer = Customer.of(CustomerId.of(1L), "Cliente Demo");

        Order order = Order.create(OrderNumber.of("ORD-2026-002"), customer, List.of(item1, item2), "admin");

        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("100");
        assertThat(order.getDomainEvents())
                .filteredOn(e -> e instanceof OrderCreated)
                .hasSize(1);
    }

    @Test
    void create_rechazaItemsConMonedasDistintas() {
        OrderItem usdItem = OrderItem.from(productoDemo("MOU-001", "Mouse", 20, USD), Quantity.of(1));
        OrderItem eurItem = OrderItem.from(
                productoDemo("KEY-001", "Teclado", 60, Currency.getInstance("EUR")), Quantity.of(1));
        Customer customer = Customer.of(CustomerId.of(1L), "Cliente Demo");

        // Order.create() no valida monedas explícitamente: la excepción la
        // lanza Money.add() al sumar subtotales de monedas distintas dentro
        // de sumItems(). Sigue siendo IllegalArgumentException, solo cambia el origen.
        assertThatThrownBy(() ->
                Order.create(OrderNumber.of("ORD-2026-003"), customer, List.of(usdItem, eurItem), "admin")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void orderItem_from_congelaSnapshotDeNombreYPrecio() {
        Product product = productoDemo("MOU-001", "Mouse", 20, USD);
        OrderItem item = OrderItem.from(product, Quantity.of(1));

        product.changePrice(Money.of(999, USD)); // cambio posterior en el producto

        assertThat(item.getUnitPrice().amount()).isEqualByComparingTo("20"); // snapshot no afectado
        assertThat(item.getProductName()).isEqualTo("Mouse");
    }

    @Test
    void cicloDeVidaCompleto_confirmShipDeliver() {
        Order order = ordenPendienteDemo();

        order.confirm();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.confirmed());

        order.ship();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.shipped());

        order.deliver();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.delivered());
    }

    @Test
    void cancel_esValidoDesdePending() {
        Order order = ordenPendienteDemo();

        order.cancel("cliente arrepentido");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.cancelled());
    }

    @Test
    void cancel_esValidoDesdeConfirmed() {
        Order order = ordenPendienteDemo();
        order.confirm();

        order.cancel("problema de inventario");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.cancelled());
    }

    @Test
    void cancel_lanzaExcepcionDesdeDelivered() {
        Order order = ordenPendienteDemo();
        order.confirm();
        order.ship();
        order.deliver();

        assertThatThrownBy(() -> order.cancel("tarde"))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Test de regresión / documentación de comportamiento actual.
     * addItem()/removeItem() NO restringen el estado a PENDING a nivel de
     * código (a pesar de que el comentario en DomainFlowDemo sugiere lo
     * contrario). Este test fija el comportamiento actual: si se decide
     * agregar la validación, este test debe actualizarse conscientemente,
     * no romperse por accidente.
     */
    @Test
    void addItem_actualmenteNoRestringeElEstadoAPending() {
        Order order = ordenPendienteDemo();
        order.confirm();
        order.ship(); // estado SHIPPED

        OrderItem nuevoItem = OrderItem.from(productoDemo("CAB-001", "Cable", 10, USD), Quantity.of(1));

        // Comportamiento actual: no lanza excepción.
        order.addItem(nuevoItem);

        assertThat(order.getItems()).contains(nuevoItem);
    }

    private Order ordenPendienteDemo() {
        OrderItem item = OrderItem.from(productoDemo("MOU-001", "Mouse", 20, USD), Quantity.of(2));
        Customer customer = Customer.of(CustomerId.of(1L), "Cliente Demo");
        return Order.create(OrderNumber.of("ORD-2026-000"), customer, List.of(item), "admin");
    }

    private Product productoDemo(String sku, String nombre, double precio, Currency currency) {
        return Product.create(
                SKU.of(sku), // debe cumplir el patrón [A-Z]+-\d{3}, p.ej. "MOU-001"
                ProductName.of(nombre),
                "desc",
                Money.of(precio, currency),
                Stock.of(100),
                CategoryReference.of("cat-1"),
                ProductImage.of("http://img/x.png"),
                "admin"
        );
    }
}
```

---

## 5. Building blocks base (`common`)

### `EntityTest.java`

```java
package com.SpringBoot.domain.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityTest {

    // Subclase mínima de prueba
    static class DummyEntity extends Entity<String> {
        private final String id;
        private String otherField;

        DummyEntity(String id, String otherField) {
            this.id = id;
            this.otherField = otherField;
        }

        @Override
        public String getId() {
            return id;
        }
    }

    @Test
    void equals_esBasadoUnicamenteEnId() {
        DummyEntity a = new DummyEntity("1", "campoA");
        DummyEntity b = new DummyEntity("1", "campoDistinto");

        assertThat(a).isEqualTo(b); // mismo id, campos distintos -> iguales
    }

    @Test
    void equals_esFalsoConIdsDistintos() {
        DummyEntity a = new DummyEntity("1", "x");
        DummyEntity b = new DummyEntity("2", "x");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void hashCode_esConsistenteConId() {
        DummyEntity a = new DummyEntity("1", "x");
        DummyEntity b = new DummyEntity("1", "y");

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
```

### `AggregateRootTest.java`

```java
package com.SpringBoot.domain.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AggregateRootTest {

    record DummyEvent(String data) implements DomainEvent {}

    static class DummyAggregate extends AggregateRoot<String> {
        private final String id;

        DummyAggregate(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        void doSomething() {
            registerEvent(new DummyEvent("algo pasó"));
        }
    }

    @Test
    void registerEvent_acumulaEventosInternos() {
        DummyAggregate aggregate = new DummyAggregate("1");

        aggregate.doSomething();
        aggregate.doSomething();

        assertThat(aggregate.getDomainEvents()).hasSize(2);
    }

    @Test
    void clearDomainEvents_vaciaLaLista() {
        DummyAggregate aggregate = new DummyAggregate("1");
        aggregate.doSomething();

        aggregate.clearDomainEvents();

        assertThat(aggregate.getDomainEvents()).isEmpty();
    }
}
```

---

## 6. Repositorios JPA (Testcontainers)

Usa Testcontainers en vez de H2 para que `ddl-auto: validate` valide contra un Postgres real
y no oculte discrepancias de schema.

> **Dos ajustes adicionales verificados al ejecutar esto contra el proyecto real
> (Spring Boot 4.1.0):**
>
> 1. **Paquetes de los slices de test cambiaron.** En Spring Boot 4.1 `@DataJpaTest`,
>    `@AutoConfigureTestDatabase` y `@DataMongoTest` ya no vienen incluidos en
>    `spring-boot-starter-test`; se movieron a starters de test independientes y
>    a paquetes nuevos:
>    - `@DataJpaTest` → `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`
>      (requiere `spring-boot-starter-data-jpa-test`)
>    - `@AutoConfigureTestDatabase` → `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase`
>    - `@DataMongoTest` → `org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest`
>      (requiere `spring-boot-starter-data-mongodb-test`)
>
> 2. **Falta una `@SpringBootConfiguration` de test.** `erp-domain` no tiene su
>    propia clase `@SpringBootApplication` (esa vive en `erp-api`), y los slices
>    de test la buscan subiendo por los paquetes desde la clase de test. Sin
>    ella falla con `IllegalStateException: Unable to find a @SpringBootConfiguration`.
>    Hace falta una clase mínima en el módulo de test, p. ej.
>    `erp-domain/src/test/java/com/SpringBoot/domain/TestApplication.java`:
>    ```java
>    package com.SpringBoot.domain;
>
>    import org.springframework.boot.SpringBootConfiguration;
>    import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
>
>    @SpringBootConfiguration
>    @EnableAutoConfiguration
>    public class TestApplication {
>    }
>    ```

### `OrderRepositoryIT.java`

```java
package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderProduct;
import com.SpringBoot.domain.entity.OrderStatus;
import com.SpringBoot.domain.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class OrderRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findByIdWithItems_traeOrderProductsSinLazyInitializationException() {
        Product product = productRepository.save(Product.builder()
                .sku("SKU-TEST")
                .name("Producto Test")
                .price(BigDecimal.TEN)
                .stock(5)
                .build());

        Order order = orderRepository.save(Order.builder()
                .orderNumber("ORD-001")
                .customerId(1L)
                .customerName("Cliente Test")
                .createdBy("system")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.TEN)
                .build());

        order.addOrderProduct(OrderProduct.builder()
                .product(product)
                .productName(product.getName())
                .quantity(1)
                .unitPrice(BigDecimal.TEN)
                .subtotal(BigDecimal.TEN)
                .build());
        orderRepository.save(order);

        Optional<Order> found = orderRepository.findByIdWithItems(order.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getOrderProducts()).hasSize(1); // no lanza LazyInitializationException
    }

    @Test
    void findByOrderNumber_encuentraPorNumeroUnico() {
        orderRepository.save(Order.builder()
                .orderNumber("ORD-UNICO")
                .customerId(1L)
                .customerName("Cliente")
                .createdBy("system")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ONE)
                .build());

        Optional<Order> found = orderRepository.findByOrderNumber("ORD-UNICO");

        assertThat(found).isPresent();
    }

    @Test
    void findByStatus_filtraPorEnumTipado() {
        orderRepository.save(Order.builder()
                .orderNumber("ORD-STATUS")
                .customerId(1L)
                .customerName("Cliente")
                .createdBy("system")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.ONE)
                .build());

        var results = orderRepository.findByStatus(OrderStatus.CONFIRMED);

        assertThat(results).isNotEmpty();
    }
}
```

> Repite el mismo patrón para `ProductRepositoryIT` (`existsBySku`, `findByActiveTrue`,
> `findByCategoryId`) y `OrderProductRepositoryIT` (`findByOrderId`, `findByProductId`).

---

## 7. Repositorios MongoDB (Testcontainers)

### `ProductDocumentRepositoryIT.java`

```java
package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.document.ProductDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
class ProductDocumentRepositoryIT {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private ProductDocumentRepository productDocumentRepository;

    @Test
    void sku_esUnicoEIndexado() {
        productDocumentRepository.save(ProductDocument.builder()
                .sku("SKU-MONGO-1")
                .name("Laptop Gamer")
                .price(BigDecimal.valueOf(1500))
                .active(true)
                .build());

        Optional<ProductDocument> found = productDocumentRepository.findBySku("SKU-MONGO-1");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Laptop Gamer");
    }

    @Test
    void findByCategoryId_filtraPorCategoria() {
        productDocumentRepository.save(ProductDocument.builder()
                .sku("SKU-MONGO-2")
                .name("Teclado Mecánico RGB")
                .description("Switches azules, retroiluminado")
                .price(BigDecimal.valueOf(80))
                .categoryId("cat-perifericos")
                .active(true)
                .build());

        var results = productDocumentRepository.findByCategoryId("cat-perifericos");

        assertThat(results).hasSize(1);
    }
}
```

> **Ajuste verificado contra el código real:** `ProductDocumentRepository` (en
> `repository/ProductDocumentRepository.java`) **no** declara ningún método de
> búsqueda full-text (`findAllBy(TextCriteria...)` no existe, aunque
> `ProductDocument` sí tiene campos `@TextIndexed` en `name`/`description`).
> El test original de este documento no compilaría contra el código actual;
> se reemplazó por `findByCategoryId`, que sí existe. Si se quiere probar
> búsqueda de texto, primero hay que declarar el método en el repositorio
> (p. ej. `List<ProductDocument> findAllBy(TextCriteria criteria)`) y luego
> escribir el test.

> Repite el patrón para `CatalogRepositoryIT` (unicidad de `catalogType`) y
> `AuditLogRepositoryIT` (queries por `userId`/`timestamp` indexados).

---

### ⚠️ Limitación conocida: Testcontainers no ejecuta en esta máquina Windows

**Estado:** `OrderRepositoryIT` y `ProductDocumentRepositoryIT` están creados,
compilan correctamente y siguen la firma real de entidades/repositorios. Pero
**no logran ejecutarse en este entorno de desarrollo** (Windows + Docker Desktop
4.74.0). No es un defecto del código de los tests.

**Causa raíz confirmada:** Docker Desktop está corriendo bien (`docker info` vía
CLI responde con datos reales, motor Engine 29.4.3 / API 1.54). Pero
`docker-java` 3.4.0 (la librería que usa Testcontainers 1.20.4 internamente)
recibe una respuesta "stub" casi vacía con `Status 400` al consultar `/info`,
tanto por named pipe como por TCP. Apunta a una incompatibilidad entre la API
del motor (1.54, muy reciente) y esa versión de `docker-java`.

**Qué se probó y descartó:**
1. Reiniciar/relanzar Docker Desktop → mismo error exacto.
2. Exponer el motor por `tcp://localhost:2375` sin TLS (bypass del named pipe)
   → mismo error exacto, confirmando que no es un problema de transporte.
3. Subir `testcontainers-bom` a la última versión (2.0.5) para traer un
   `docker-java` más nuevo → esa versión mayor reestructuró el BOM (ya no
   declara `postgresql`/`mongodb`/`junit-jupiter` de la misma forma) y migrar
   a ciegas sin poder validar el resultado no compensaba el riesgo. Se revirtió
   a `1.20.4`.

**Impacto real (bajo):**
- No afecta a la aplicación en producción: Testcontainers nunca corre fuera de
  los tests.
- No afecta a los 58 tests de dominio puro (secciones 2-5), que no dependen de
  Docker y ya se ejecutan y pasan en esta máquina.
- `erp-application`/`erp-infrastructure` siguen siendo placeholders (ver
  sección 8) — nada en el proyecto usa hoy estos repositorios de forma real,
  así que la verificación contra base de datos real queda pendiente pero no
  bloquea nada activo.
- Es específico de Docker Desktop en Windows; en CI sobre Linux (GitHub
  Actions, GitLab CI, etc.) lo más probable es que estos mismos tests corran
  sin este problema, porque ahí no existe el proxy/named pipe de Windows.

**Posibles vías a futuro (no intentadas más a fondo):**
- Probar una versión intermedia de Testcontainers 1.21.x (entre 1.20.4 y el
  salto mayor a 2.x) por si trae un `docker-java` algo más nuevo sin romper
  el BOM.
- Ejecutar estos tests en CI/Linux en vez de en esta máquina.
- Esperar una actualización de `docker-java`/Testcontainers con soporte
  explícito para Docker Desktop 4.74+/API 1.54.

---

## 7.5. Extra: lógica de negocio en `entity/` (fuera del alcance original de este doc)

Al revisar qué quedaba sin cubrir más allá de lo documentado arriba, se encontró
que `entity/Order.java` y `entity/OrderProduct.java` (las entidades JPA, **no**
los aggregates DDD de `order/`) tienen métodos con lógica real de cálculo que
**no requieren Docker ni `@DataJpaTest`** — son objetos Java planos, solo hace
falta `Order.builder()`/`OrderProduct.builder()`:

- `OrderProduct.setQuantity()` / `setUnitPrice()` → disparan `recalculateSubtotal()`
  (`unitPrice × quantity`, solo si ambos son no-null)
- `Order.addOrderProduct()` / `removeOrderProduct()` → enlazan la relación
  bidireccional y disparan `recalculateTotalAmount()` (suma de subtotales,
  tratando `null` como `BigDecimal.ZERO`)

Antes de este hallazgo, esta lógica solo se habría verificado indirectamente
vía `OrderRepositoryIT`, que está bloqueado por la limitación de Docker de la
sección anterior. Se agregaron como tests independientes:

### `entity/OrderProductTest.java`

```java
package com.SpringBoot.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductTest {

    @Test
    void builder_subtotalPorDefectoEsCero() {
        OrderProduct item = OrderProduct.builder()
                .productName("Mouse")
                .build();

        assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void setQuantity_recalculaSubtotalUsandoUnitPriceActual() {
        OrderProduct item = OrderProduct.builder()
                .productName("Mouse")
                .quantity(1)
                .unitPrice(BigDecimal.TEN)
                .build();

        item.setQuantity(3);

        assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    @Test
    void setUnitPrice_recalculaSubtotalUsandoQuantityActual() {
        OrderProduct item = OrderProduct.builder()
                .productName("Mouse")
                .quantity(4)
                .unitPrice(BigDecimal.ONE)
                .build();

        item.setUnitPrice(BigDecimal.valueOf(25));

        assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void setQuantity_noRecalculaSiUnitPriceEsNull() {
        OrderProduct item = OrderProduct.builder()
                .productName("Mouse")
                .build(); // unitPrice queda null

        item.setQuantity(5);

        // recalculateSubtotal() exige unitPrice != null && quantity != null;
        // como falta unitPrice, el subtotal se queda en el default (ZERO).
        assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void setUnitPrice_noRecalculaSiQuantityEsNull() {
        OrderProduct item = OrderProduct.builder()
                .productName("Mouse")
                .build(); // quantity queda null

        item.setUnitPrice(BigDecimal.TEN);

        assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
```

### `entity/OrderTest.java`

```java
package com.SpringBoot.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void addOrderProduct_enlazaLaOrdenYSumaElSubtotalAlTotal() {
        Order order = ordenBaseVacia();
        OrderProduct item = itemConSubtotal(20);

        order.addOrderProduct(item);

        assertThat(order.getOrderProducts()).containsExactly(item);
        assertThat(item.getOrder()).isSameAs(order);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("20");
    }

    @Test
    void addOrderProduct_conMultiplesItemsSumaTodosLosSubtotales() {
        Order order = ordenBaseVacia();

        order.addOrderProduct(itemConSubtotal(20));
        order.addOrderProduct(itemConSubtotal(35));

        assertThat(order.getTotalAmount()).isEqualByComparingTo("55");
    }

    @Test
    void removeOrderProduct_restaElSubtotalDelTotal() {
        Order order = ordenBaseVacia();
        OrderProduct item1 = itemConSubtotal(20);
        OrderProduct item2 = itemConSubtotal(35);
        order.addOrderProduct(item1);
        order.addOrderProduct(item2);

        order.removeOrderProduct(item1);

        assertThat(order.getOrderProducts()).containsExactly(item2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("35");
    }

    @Test
    void recalculateTotalAmount_tratraSubtotalNullComoCero() {
        Order order = ordenBaseVacia();
        OrderProduct itemSinSubtotal = OrderProduct.builder()
                .productName("Item sin subtotal")
                .quantity(1)
                .unitPrice(BigDecimal.TEN)
                .subtotal(null)
                .build();

        order.addOrderProduct(itemSinSubtotal);

        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Order ordenBaseVacia() {
        return Order.builder()
                .orderNumber("ORD-001")
                .customerId(1L)
                .customerName("Cliente")
                .createdBy("admin")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();
    }

    private OrderProduct itemConSubtotal(double subtotal) {
        return OrderProduct.builder()
                .productName("Item")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(subtotal))
                .subtotal(BigDecimal.valueOf(subtotal))
                .build();
    }
}
```

> **Nota:** `.subtotal(BigDecimal.TEN)`/`.subtotal(null)` vía builder se puede
> fijar directamente aunque el campo tenga `@Setter(AccessLevel.NONE)` — esa
> anotación solo suprime el setter de instancia, no el método del builder de
> Lombok. Esto permite simular en el test el estado que normalmente dejaría
> `@PrePersist`/`@PreUpdate` sin necesitar un `EntityManager` real.

**Actualización — ya escritos:** los Value Objects que antes solo se ejercitaban
por el camino feliz dentro de `ProductTest`/`OrderTest` ahora tienen su propio
test cubriendo también los casos de rechazo (formato inválido, blank, nulo,
límites de longitud):

| Test | Archivo | Qué cubre |
|---|---|---|
| `SKUTest` | `product/SKUTest.java` | patrón `[A-Z]+-\d{3}`, valor nulo |
| `ProductNameTest` | `product/ProductNameTest.java` | longitud 3-200 (incluye límites exactos), valor nulo |
| `CategoryReferenceTest` | `product/CategoryReferenceTest.java` | blank/nulo |
| `ProductImageTest` | `product/ProductImageTest.java` | URL absoluta http/https, `getFileName()`/`getFullUrl()`, blank/relativa/nulo |
| `OrderNumberTest` | `order/OrderNumberTest.java` | patrón `ORD-YYYY-NNN`, nulo, y que `generate()` produzca siempre un valor válido según su propio patrón |
| `CustomerTest` | `order/CustomerTest.java` | `customerId` no nulo, `customerName` no blank/nulo |
| `CustomerInfoTest` | `customer/CustomerInfoTest.java` | `id` no nulo, `name` no blank/nulo (resto de campos sin validar) |

Con esto la suite de dominio puro pasó de 67 a **116 tests**, todos sin Docker.

---

## 8. Qué NO testear todavía

| Componente | Razón |
|---|---|
| `erp-application` (`ApplicationService`) | Placeholder sin lógica real — nada que testear |
| `erp-infrastructure` (`BaseRepository`) | Placeholder — esperar implementación concreta |
| `HealthController` | Trivial; un test de integración liviano basta cuando existan más controllers |
| `CustomerProvider` (puerto) | Es una interfaz — testear su implementación concreta (cliente HTTP) cuando exista en `erp-infrastructure`, con `WireMock` o similar |

---

## 9. Dependencias necesarias

> **Estado real aplicado a `erp-domain/pom.xml`** (verificado compilando y
> ejecutando contra el proyecto, Spring Boot 4.1.0): además de Testcontainers,
> hacen falta los starters de test-slice `spring-boot-starter-data-jpa-test` y
> `spring-boot-starter-data-mongodb-test` — en esta versión de Spring Boot
> `@DataJpaTest`/`@DataMongoTest` **no** vienen incluidos en
> `spring-boot-starter-test` (ver nota en la sección 6). Se necesita también un
> `dependencyManagement` con el BOM de Testcontainers para fijar versiones.

En `erp-domain/pom.xml`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-bom</artifactId>
            <version>1.20.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Necesarios en Spring Boot 4.1 para @DataJpaTest / @DataMongoTest -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mongodb</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

`spring-boot-starter-test` ya incluye JUnit 5, AssertJ y Mockito — suficiente para las
secciones 2 a 5. Testcontainers y los starters de test-slice solo hacen falta para
las secciones 6 y 7.

---

## Resumen ejecutable

```bash
# Correr solo tests de dominio puro (rápidos, sin Docker)
mvn -pl erp-domain test -Dtest=*ProductTest,*OrderTest,*MoneyTest,*StockTest,*QuantityTest

# Correr suite completa incluyendo Testcontainers (requiere Docker activo)
mvn -pl erp-domain test
```
