package com.SpringBoot.domain.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductNameTest {

    @Test
    void of_aceptaNombreDeLongitudValida() {
        assertThat(ProductName.of("Laptop").value()).isEqualTo("Laptop");
    }

    @ParameterizedTest
    @ValueSource(strings = {"AB", "A", ""})
    void of_rechazaNombreMasCortoDeTresCaracteres(String value) {
        assertThatThrownBy(() -> ProductName.of(value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_rechazaNombreMasLargoDe200Caracteres() {
        String nombreLargo = "A".repeat(201);

        assertThatThrownBy(() -> ProductName.of(nombreLargo))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_aceptaNombreDeExactamente200Caracteres() {
        String nombreLimite = "A".repeat(200);

        assertThat(ProductName.of(nombreLimite).value()).hasSize(200);
    }

    @Test
    void of_rechazaValorNulo() {
        assertThatThrownBy(() -> ProductName.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
