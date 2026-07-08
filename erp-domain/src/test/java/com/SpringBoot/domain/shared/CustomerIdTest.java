package com.SpringBoot.domain.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
