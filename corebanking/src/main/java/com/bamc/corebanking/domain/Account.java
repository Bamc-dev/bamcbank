package com.bamc.corebanking.domain;

import com.bamc.corebanking.domain.exception.InsufficientFundsException;
import java.util.List;
import java.util.ArrayList;

public class Account {

    private Amount balance;
    private List<Amount> reservations;

    private Account(Amount balance) {
        this.balance = balance;
        this.reservations = new ArrayList<>();
    }

    public static Account open(Amount balance) {
        return new Account(balance);
    }

    public Amount balance() {
        return this.balance;
    }

    public Amount availableBalance() {
        return this.balance.subtract(this.reservations.stream().reduce(Amount.of("0.00"), Amount::add));
    }

    public void reserve(Amount amount) {
        if (amount.isGreaterThan(this.availableBalance())) {
            throw new InsufficientFundsException(amount, this.availableBalance());
        }
        this.reservations.add(amount);
    }

}
