package com.SpringBoot.domain.shared;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {"user@example.com", "a.b+c@sub.example.co"})
    void of_aceptaEmailsValidos(String email) {
        assertThat(Email.of(email).value()).isEqualTo(email);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalido", "sin-arroba.com", "@sin-usuario.com", "user@"})
    void of_rechazaEmailsInvalidos(String email) {
        assertThatThrownBy(() -> Email.of(email))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
