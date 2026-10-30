package com.bamc.corebanking.domain.exception;

public class TransferNotExecutableException extends DomainException {

    public TransferNotExecutableException() {
        super("Cannot execute transfer");
    }

}
