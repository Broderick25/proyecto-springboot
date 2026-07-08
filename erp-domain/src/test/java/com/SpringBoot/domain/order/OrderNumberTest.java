package com.SpringBoot.domain.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderNumberTest {

    @ParameterizedTest
    @ValueSource(strings = {"ORD-2026-001", "ORD-1999-999"})
    void of_aceptaFormatoValido(String value) {
        assertThat(OrderNumber.of(value).value()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ORD-26-001", "ORD-2026-01", "2026-001", "ord-2026-001", "ORD-2026-0001"})
    void of_rechazaFormatoInvalido(String value) {
        assertThatThrownBy(() -> OrderNumber.of(value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_rechazaValorNulo() {
        assertThatThrownBy(() -> OrderNumber.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generate_produceUnValorQueCumpleElPatronPropio() {
        OrderNumber generated = OrderNumber.generate();

        // Si generate() alguna vez produjera un valor que no cumple su propio
        // patrón, OrderNumber.of() lo rechazaría; este test lo detecta aquí
        // en vez de en producción.
        assertThat(OrderNumber.of(generated.value())).isEqualTo(generated);
    }
}
