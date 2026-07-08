package com.SpringBoot.domain.order;

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
