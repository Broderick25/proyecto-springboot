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
