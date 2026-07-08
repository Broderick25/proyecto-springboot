package com.SpringBoot.domain.shared;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Money amount must not be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Money currency must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money amount must not be negative");
        }
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money subtraction result must not be negative");
        }
        return new Money(result, this.currency);
    }

    public Money multiply(int multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("multiplier must not be negative");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    public Money multiply(Quantity quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("quantity must not be null");
        }
        return multiply(quantity.value());
    }

    private void validateSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("other Money must not be null");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot operate on Money with different currencies: " + this.currency + " vs " + other.currency);
        }
    }
}
