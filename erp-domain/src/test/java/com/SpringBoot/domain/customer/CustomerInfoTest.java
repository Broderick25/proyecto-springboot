package com.SpringBoot.domain.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerInfoTest {

    @Test
    void constructor_creaInstanciaValidaConIdYNamePresentes() {
        CustomerInfo info = new CustomerInfo(1L, "María García", "maria@example.com",
                "555-1234", "Calle 1", "Springfield", "00000", "ACME");

        assertThat(info.id()).isEqualTo(1L);
        assertThat(info.name()).isEqualTo("María García");
    }

    @Test
    void constructor_rechazaIdNulo() {
        assertThatThrownBy(() ->
                new CustomerInfo(null, "María García", null, null, null, null, null, null)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void constructor_rechazaNameBlank(String nombre) {
        assertThatThrownBy(() ->
                new CustomerInfo(1L, nombre, null, null, null, null, null, null)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_rechazaNameNulo() {
        assertThatThrownBy(() ->
                new CustomerInfo(1L, null, null, null, null, null, null, null)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
