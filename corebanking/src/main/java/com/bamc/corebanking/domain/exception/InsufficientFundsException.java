package com.bamc.corebanking.domain.exception;

import com.bamc.corebanking.domain.Amount;

public class InsufficientFundsException extends DomainException {

    public InsufficientFundsException(Amount requested, Amount available) {
        super("Insufficient funds: requested " + requested + "€, available " + available + "€");
    }
}
