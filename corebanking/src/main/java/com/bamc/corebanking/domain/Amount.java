package com.bamc.corebanking.domain;

import java.math.BigDecimal;

public record Amount(BigDecimal value) {

    public Amount {
        if (value == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        try {
            value = value.setScale(2);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Amount cannot have more than two decimals", exception);
        }
    }

    public static Amount of(String string) {
        if (string == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        return new Amount(new BigDecimal(string));
    }

    public Amount add(Amount other) {
        return new Amount(this.value.add(other.value));
    }

    public Amount subtract(Amount other) {
        return new Amount(this.value.subtract(other.value));
    }

    public boolean isGreaterThan(Amount of) {
        return this.value.compareTo(of.value) > 0;
    }

    public boolean isLessThan(Amount of) {
        return this.value.compareTo(of.value) < 0;
    }

    @Override
    public String toString() {
        return this.value.toPlainString();
    }

}
