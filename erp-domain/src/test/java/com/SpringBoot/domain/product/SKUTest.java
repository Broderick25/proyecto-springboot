package com.SpringBoot.domain.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SKUTest {

    @ParameterizedTest
    @ValueSource(strings = {"LAPTOP-001", "A-000", "MOUSE-042"})
    void of_aceptaFormatoValido(String value) {
        assertThat(SKU.of(value).value()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"laptop-001", "LAPTOP001", "LAPTOP-01", "LAPTOP-0001", "SKU_001", ""})
    void of_rechazaFormatoInvalido(String value) {
        assertThatThrownBy(() -> SKU.of(value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_rechazaValorNulo() {
        assertThatThrownBy(() -> SKU.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
