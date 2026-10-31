package com.bamc.corebanking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.bamc.corebanking.domain.exception.DomainException;
import com.bamc.corebanking.domain.exception.InsufficientFundsException;
import com.bamc.corebanking.domain.exception.ReservationNotFoundException;

class AccountTest {

    // --- Creation ---

    @Test
    void newAccountHasAnAvailableBalanceEqualToItsBalance() {
        Account account = Account.open(Amount.of("300.00"));

        assertThat(account.balance()).isEqualTo(account.availableBalance());
        assertThat(account.availableBalance()).isEqualTo(Amount.of("300.00"));
    }

    // --- Reservation ---

    @Test
    void reservingReducesTheAvailableBalanceButNotTheBalance() {
        Account account = Account.open(Amount.of("300.00"));

        account.reserve(Amount.of("250.00"));

        assertThat(account.balance()).isEqualTo(Amount.of("300.00"));
        assertThat(account.availableBalance()).isEqualTo(Amount.of("50.00"));
    }

    @Test
    void canReserveTheWholeAvailableBalance() {
        Account account = Account.open(Amount.of("300.00"));

        account.reserve(Amount.of("300.00"));

        assertThat(account.availableBalance()).isEqualTo(Amount.of("0.00"));
    }

    @Test
    void successiveReservationsAreCumulated() {
        Account account = Account.open(Amount.of("300.00"));

        account.reserve(Amount.of("100.00"));
        account.reserve(Amount.of("150.00"));

        assertThat(account.balance()).isEqualTo(Amount.of("300.00"));
        assertThat(account.availableBalance()).isEqualTo(Amount.of("50.00"));
    }

    // --- Settlement ---

    @Test
    void settlingAPresentReservationRemovesItAndDecreasesTheBalance() {
        Account account = Account.open(Amount.of("300.00"));
        account.reserve(Amount.of("250.00"));

        account.settleReservation(Amount.of("250.00"));

        // the hold is gone, so the available balance equals the new balance
        assertThat(account.balance()).isEqualTo(Amount.of("50.00"));
        assertThat(account.availableBalance()).isEqualTo(Amount.of("50.00"));
    }

    @Test
    void settlingAnAbsentReservationIsRefusedAndChangesNothing() {
        Account account = Account.open(Amount.of("300.00"));

        assertThatThrownBy(() -> account.settleReservation(Amount.of("250.00")))
                .isInstanceOf(ReservationNotFoundException.class);

        assertThat(account.balance()).isEqualTo(Amount.of("300.00"));
        assertThat(account.availableBalance()).isEqualTo(Amount.of("300.00"));
    }

    // --- Release ---

    @Test
    void releasingAPresentReservationRestoresTheAvailableBalanceAndKeepsTheBalance() {
        Account account = Account.open(Amount.of("300.00"));
        account.reserve(Amount.of("250.00"));

        account.releaseReservation(Amount.of("250.00"));

        assertThat(account.balance()).isEqualTo(Amount.of("300.00"));
        assertThat(account.availableBalance()).isEqualTo(Amount.of("300.00"));
    }

    // --- Insufficient funds ---

    @Test
    void rejectsAReservationGreaterThanTheAvailableBalance() {
        Account account = Account.open(Amount.of("300.00"));

        assertThatThrownBy(() -> account.reserve(Amount.of("300.01")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void rejectsAReservationThatExceedsWhatPreviousReservationsLeftAvailable() {
        // 250 + 100 > 300: the check uses the available balance, not the balance
        Account account = Account.open(Amount.of("300.00"));
        account.reserve(Amount.of("250.00"));

        assertThatThrownBy(() -> account.reserve(Amount.of("100.00")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void aRejectedReservationChangesNothing() {
        Account account = Account.open(Amount.of("300.00"));
        account.reserve(Amount.of("250.00"));

        assertThatThrownBy(() -> account.reserve(Amount.of("100.00")))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(account.balance()).isEqualTo(Amount.of("300.00"));
        assertThat(account.availableBalance()).isEqualTo(Amount.of("50.00"));
    }

    // --- Business exceptions ---

    @Test
    void insufficientFundsIsABusinessException() {
        Account account = Account.open(Amount.of("10.00"));

        assertThatThrownBy(() -> account.reserve(Amount.of("20.00")))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void insufficientFundsMessageTellsTheRequestedAndAvailableAmounts() {
        Account account = Account.open(Amount.of("10.00"));

        assertThatThrownBy(() -> account.reserve(Amount.of("20.00")))
                .hasMessageContaining("20.00")
                .hasMessageContaining("10.00");
    }


}
