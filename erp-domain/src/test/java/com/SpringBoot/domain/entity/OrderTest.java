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
