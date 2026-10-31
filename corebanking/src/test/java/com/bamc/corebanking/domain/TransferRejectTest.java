package com.bamc.corebanking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.bamc.corebanking.domain.enums.TransferState;
import com.bamc.corebanking.domain.events.TransferRejected;
import com.bamc.corebanking.domain.exception.TransferNotExecutableException;
import com.bamc.corebanking.domain.exception.TransferNotRejectableException;

class TransferRejectTest {

    // --- Successful rejection ---

    @Test
    void rejectingATransferLeavesTheSenderBalanceUnchanged() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));

        transfer.reject();

        assertThat(sender.balance()).isEqualTo(Amount.of("300.00"));
    }

    @Test
    void rejectingATransferReleasesTheReservation() {
        // the money never left: the hold is lifted so the whole balance is available again
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));

        transfer.reject();

        assertThat(sender.availableBalance()).isEqualTo(Amount.of("300.00"));
    }

    @Test
    void rejectingATransferNeverCreditsTheReceiver() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));

        transfer.reject();

        assertThat(receiver.balance()).isEqualTo(Amount.of("0.00"));
    }

    @Test
    void aRejectedTransferIsInStateRejectedFraud() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));

        transfer.reject();

        assertThat(transfer.state()).isEqualTo(TransferState.REJECTED_FRAUD);
    }

    // --- Domain event ---

    @Test
    void rejectingATransferProducesATransferRejectedEvent() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));

        TransferRejected event = transfer.reject();

        assertThat(event.amount()).isEqualTo(Amount.of("250.00"));
    }

    // --- Refused rejections ---

    @Test
    void rejectingAnAlreadyExecutedTransferIsRefused() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));
        transfer.execute();

        assertThatThrownBy(transfer::reject)
                .isInstanceOf(TransferNotRejectableException.class);
    }

    @Test
    void rejectingAnAlreadyRejectedTransferIsRefused() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));
        transfer.reject();

        assertThatThrownBy(transfer::reject)
                .isInstanceOf(TransferNotRejectableException.class);
    }

    @Test
    void executingARejectedTransferIsRefusedAndMovesNoMoney() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));
        transfer.reject();

        assertThatThrownBy(transfer::execute)
                .isInstanceOf(TransferNotExecutableException.class);

        assertThat(transfer.state()).isEqualTo(TransferState.REJECTED_FRAUD);
        assertThat(sender.balance()).isEqualTo(Amount.of("300.00"));
        assertThat(receiver.balance()).isEqualTo(Amount.of("0.00"));
    }

    @Test
    void rejectingAnAlreadyExecutedTransferChangesNothing() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));
        transfer.execute();

        assertThatThrownBy(transfer::reject)
                .isInstanceOf(TransferNotRejectableException.class);

        assertThat(transfer.state()).isEqualTo(TransferState.EXECUTED);
        assertThat(sender.balance()).isEqualTo(Amount.of("50.00"));
        assertThat(receiver.balance()).isEqualTo(Amount.of("250.00"));
    }
}
