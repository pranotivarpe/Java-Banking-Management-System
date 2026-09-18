package com.pranotivarpe.bankingsystem.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Integer accountNumber, BigDecimal balance, BigDecimal requested) {
        super("Account " + accountNumber + " has insufficient funds. Balance: " + balance
                + ", requested: " + requested);
    }
}
