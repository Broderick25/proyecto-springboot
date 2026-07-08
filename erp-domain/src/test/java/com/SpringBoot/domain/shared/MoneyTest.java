package com.SpringBoot.domain.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private final Currency USD = Currency.getInstance("USD");
    private final Currency EUR = Currency.getInstance("EUR");

    @Test
    void of_creaMoneyValidoConMontoPositivo() {
        Money money = Money.of(1299.99, USD);

        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.valueOf(1299.99));
        assertThat(money.currency()).isEqualTo(USD);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -100.0})
    void constructor_rechazaMontoNegativo(double montoInvalido) {
        assertThatThrownBy(() -> Money.of(montoInvalido, USD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void add_sumaCorrectamenteConMismaMoneda() {
        Money a = Money.of(100, USD);
        Money b = Money.of(50, USD);

        Money result = a.add(b);

        assertThat(result.amount()).isEqualByComparingTo("150");
    }

    @Test
    void add_lanzaExcepcionConMonedasDistintas() {
        Money usd = Money.of(100, USD);
        Money eur = Money.of(50, EUR);

        assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void subtract_noPermiteResultadoNegativo() {
        Money a = Money.of(50, USD);
        Money b = Money.of(100, USD);

        assertThatThrownBy(() -> a.subtract(b))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void multiply_porQuantityNoMutaElOriginal() {
        Money price = Money.of(1299.99, USD);

        Money total = price.multiply(Quantity.of(3));

        assertThat(price.amount()).isEqualByComparingTo("1299.99"); // no mutado
        assertThat(total.amount()).isEqualByComparingTo("3899.97");
    }
}
