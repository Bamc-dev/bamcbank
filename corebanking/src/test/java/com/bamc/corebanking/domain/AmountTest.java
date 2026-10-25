package com.bamc.corebanking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AmountTest {

    // --- Creation ---

    @Test
    void createsAnAmountFromAPositiveValue() {
        assertThat(Amount.of("10.50")).hasToString("10.50");
    }

    @Test
    void rejectsANegativeAmount() {
        assertThatThrownBy(() -> Amount.of("-1.00"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsANullAmount() {
        assertThatThrownBy(() -> Amount.of((String) null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- Equality ---

    @Test
    void twoAmountsWithTheSameValueAreEqual() {
        assertThat(Amount.of("10.00")).isEqualTo(Amount.of("10.00"));
    }

    @Test
    void equalityIgnoresHowTheValueIsWritten() {
        assertThat(Amount.of("10")).isEqualTo(Amount.of("10.00"));
    }

    @Test
    void twoAmountsWithDifferentValuesAreNotEqual() {
        assertThat(Amount.of("10.00")).isNotEqualTo(Amount.of("10.01"));
    }

    // --- Arithmetic ---

    @Test
    void addsTwoAmounts() {
        assertThat(Amount.of("10.00").add(Amount.of("5.50"))).isEqualTo(Amount.of("15.50"));
    }

    @Test
    void subtractsTwoAmounts() {
        assertThat(Amount.of("10.00").subtract(Amount.of("5.50"))).isEqualTo(Amount.of("4.50"));
    }

    // --- Cents precision ---

    @Test
    void doesNotLoseCentsWhenAdding() {
        // With double, 0.10 + 0.20 gives 0.30000000000000004
        assertThat(Amount.of("0.10").add(Amount.of("0.20"))).isEqualTo(Amount.of("0.30"));
    }

    @Test
    void rejectsAValueSmallerThanOneCent() {
        // Silently rounding 10.005 would lose money: refuse it instead
        assertThatThrownBy(() -> Amount.of("10.005"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- Comparison ---
    @Test
    void anAmountIsGreaterThanAnotherIfItsValueIsGreater() {
        assertThat(Amount.of("10.01").compareTo(Amount.of("10.00")) > 0);
    }

    @Test
    void anAmountIsLessThanAnotherIfItsValueIsLess() {
        assertThat(Amount.of("10.00").compareTo(Amount.of("10.01")) < 0);
    }
}
