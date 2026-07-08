package com.SpringBoot.domain.product;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    @Test
    void increment_devuelveNuevaInstanciaInmutable() {
        Stock original = Stock.of(10);

        Stock incremented = original.increment(5);

        assertThat(original.value()).isEqualTo(10);       // no mutado
        assertThat(incremented.value()).isEqualTo(15);
    }

    @Test
    void decrement_devuelveNuevaInstanciaInmutable() {
        Stock original = Stock.of(10);

        Stock decremented = original.decrement(3);

        assertThat(original.value()).isEqualTo(10);
        assertThat(decremented.value()).isEqualTo(7);
    }

    @Test
    void decrement_noPermiteResultadoNegativo() {
        Stock stock = Stock.of(5);

        assertThatThrownBy(() -> stock.decrement(10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hasAvailable_retornaTrueSiHayStockSuficiente() {
        Stock stock = Stock.of(10);

        assertThat(stock.hasAvailable(5)).isTrue();
        assertThat(stock.hasAvailable(15)).isFalse();
    }
}
