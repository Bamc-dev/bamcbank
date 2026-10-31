package com.bamc.corebanking.domain.exception;

public class TransferNotRejectableException extends DomainException {

    public TransferNotRejectableException() {
        super("Cannot reject transfer");
    }

}
