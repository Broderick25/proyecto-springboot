package com.SpringBoot.domain.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryReferenceTest {

    @Test
    void of_aceptaValorNoBlank() {
        assertThat(CategoryReference.of("cat-electronics").categoryId()).isEqualTo("cat-electronics");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void of_rechazaValorBlank(String value) {
        assertThatThrownBy(() -> CategoryReference.of(value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_rechazaValorNulo() {
        assertThatThrownBy(() -> CategoryReference.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
