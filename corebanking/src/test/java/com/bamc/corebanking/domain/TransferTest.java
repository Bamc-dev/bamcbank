package com.bamc.corebanking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.bamc.corebanking.domain.enums.TransferState;
import com.bamc.corebanking.domain.exception.InsufficientFundsException;

class TransferTest {

    // --- Creation ---

    @Test
    void newTransferIsPendingFraudCheck() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));

        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));

        assertThat(transfer.state()).isEqualTo(TransferState.PENDING_FRAUD_CHECK);
        assertThat(transfer.amount()).isEqualTo(Amount.of("250.00"));
    }

    @Test
    void creatingATransferReservesTheAmountOnTheDebitedAccount() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));

        Transfer.create(sender, receiver, Amount.of("250.00"));

        assertThat(sender.balance()).isEqualTo(Amount.of("300.00"));
        assertThat(sender.availableBalance()).isEqualTo(Amount.of("50.00"));
    }

    @Test
    void creatingATransferDoesNotCreditTheReceiver() {
        // the receiver is only credited when the transfer is executed
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("10.00"));

        Transfer.create(sender, receiver, Amount.of("250.00"));

        assertThat(receiver.balance()).isEqualTo(Amount.of("10.00"));
        assertThat(receiver.availableBalance()).isEqualTo(Amount.of("10.00"));
    }

    @Test
    void canTransferTheWholeAvailableBalance() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));

        Transfer transfer = Transfer.create(sender, receiver, Amount.of("300.00"));

        assertThat(transfer.state()).isEqualTo(TransferState.PENDING_FRAUD_CHECK);
        assertThat(sender.availableBalance()).isEqualTo(Amount.of("0.00"));
    }

    // --- Insufficient funds ---

    @Test
    void insufficientFundsRejectsTheTransferImmediately() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));

        assertThatThrownBy(() -> Transfer.create(sender, receiver, Amount.of("300.01")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void aRejectedTransferReservesNothing() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));

        assertThatThrownBy(() -> Transfer.create(sender, receiver, Amount.of("300.01")))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(sender.balance()).isEqualTo(Amount.of("300.00"));
        assertThat(sender.availableBalance()).isEqualTo(Amount.of("300.00"));
    }

    // --- Two transfers on the same account ---

    @Test
    void secondTransferIsRejectedBecauseTheFirstOneReservedTheMoney() {
        // 250 + 250 > 300: the check uses the available balance (50), not the balance (300)
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer first = Transfer.create(sender, receiver, Amount.of("250.00"));

        assertThatThrownBy(() -> Transfer.create(sender, receiver, Amount.of("250.00")))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(first.state()).isEqualTo(TransferState.PENDING_FRAUD_CHECK);
        assertThat(sender.availableBalance()).isEqualTo(Amount.of("50.00"));
    }
}
