package com.SpringBoot.domain.order;

import com.SpringBoot.domain.order.events.OrderCreated;
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
