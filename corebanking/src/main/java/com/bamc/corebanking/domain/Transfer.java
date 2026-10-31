package com.bamc.corebanking.domain;

import com.bamc.corebanking.domain.enums.TransferState;
import com.bamc.corebanking.domain.events.TransferExecuted;
import com.bamc.corebanking.domain.exception.TransferNotExecutableException;

public class Transfer {

    private final Account sender;
    private final Account receiver;
    private final Amount amount;
    private TransferState state;

    private Transfer(Account sender, Account receiver, Amount amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.state = TransferState.PENDING_FRAUD_CHECK;
    }

    public static Transfer create(Account sender, Account receiver, Amount amount) {
        sender.reserve(amount);
        return new Transfer(sender, receiver, amount);
    }

    public TransferState state() {
        return state;
    }

    public Amount amount() {
        return amount;
    }

    public TransferExecuted execute() {
        if (state != TransferState.PENDING_FRAUD_CHECK) {
            throw new TransferNotExecutableException();
        }
        sender.settleReservation(amount);
        receiver.credit(amount);
        this.state = TransferState.EXECUTED;
        return new TransferExecuted(amount);
    }

}
