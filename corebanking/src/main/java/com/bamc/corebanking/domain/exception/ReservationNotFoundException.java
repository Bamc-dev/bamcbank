package com.bamc.corebanking.domain.exception;

public class ReservationNotFoundException extends DomainException {

    public ReservationNotFoundException() {
        super("The requested reservation was not found");
    }

}
