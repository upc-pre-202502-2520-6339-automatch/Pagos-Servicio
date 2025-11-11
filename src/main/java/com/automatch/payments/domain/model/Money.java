package com.automatch.payments.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {
    public Money {
        if (amount == null || amount.signum() < 0)
            throw new IllegalArgumentException("Invalid amount");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Invalid currency");
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }
}