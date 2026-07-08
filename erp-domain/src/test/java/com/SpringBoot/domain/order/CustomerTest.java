package com.SpringBoot.domain.order;

import com.SpringBoot.domain.shared.CustomerId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    @Test
    void of_creaClienteValido() {
        Customer customer = Customer.of(CustomerId.of(1L), "Cliente Demo");

        assertThat(customer.customerId()).isEqualTo(CustomerId.of(1L));
        assertThat(customer.customerName()).isEqualTo("Cliente Demo");
    }

    @Test
    void of_rechazaCustomerIdNulo() {
        assertThatThrownBy(() -> Customer.of(null, "Cliente Demo"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void of_rechazaCustomerNameBlank(String nombre) {
        assertThatThrownBy(() -> Customer.of(CustomerId.of(1L), nombre))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_rechazaCustomerNameNulo() {
        assertThatThrownBy(() -> Customer.of(CustomerId.of(1L), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
