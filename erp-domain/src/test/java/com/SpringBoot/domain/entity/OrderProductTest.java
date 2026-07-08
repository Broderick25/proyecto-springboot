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
