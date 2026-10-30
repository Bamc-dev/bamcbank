package com.bamc.corebanking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.bamc.corebanking.domain.enums.TransferState;
import com.bamc.corebanking.domain.events.TransferExecuted;
import com.bamc.corebanking.domain.exception.ReservationNotFoundException;
import com.bamc.corebanking.domain.exception.TransferNotExecutableException;

class TransferExecuteTest {

    // --- Successful execution ---

    @Test
    void executingATransferDebitsTheSenderAndCreditsTheReceiver() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));

        transfer.execute();

        assertThat(sender.balance()).isEqualTo(Amount.of("50.00"));
        assertThat(receiver.balance()).isEqualTo(Amount.of("250.00"));
    }

    @Test
    void executingATransferReleasesTheReservation() {
        // the reservation is consumed by the debit: it must not be counted a second
        // time
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));

        transfer.execute();

        assertThat(sender.availableBalance()).isEqualTo(Amount.of("50.00"));
    }

    @Test
    void anExecutedTransferIsInStateExecuted() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));

        transfer.execute();

        assertThat(transfer.state()).isEqualTo(TransferState.EXECUTED);
    }

    // --- Domain event ---

    @Test
    void executingATransferProducesATransferExecutedEvent() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));

        TransferExecuted event = transfer.execute();

        assertThat(event.amount()).isEqualTo(Amount.of("250.00"));
    }

    // --- Refused executions ---

    @Test
    void executingAnAlreadyExecutedTransferIsRefused() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));
        transfer.execute();

        assertThatThrownBy(transfer::execute)
                .isInstanceOf(TransferNotExecutableException.class);
    }

    @Test
    void executingAnAlreadyExecutedTransferDoesNotMoveMoneyAgain() {
        Account sender = Account.open(Amount.of("300.00"));
        Account receiver = Account.open(Amount.of("0.00"));
        Transfer transfer = Transfer.create(sender, receiver, Amount.of("250.00"));
        transfer.execute();

        assertThatThrownBy(transfer::execute)
                .isInstanceOf(TransferNotExecutableException.class);

        assertThat(sender.balance()).isEqualTo(Amount.of("50.00"));
        assertThat(receiver.balance()).isEqualTo(Amount.of("250.00"));
    }
}
