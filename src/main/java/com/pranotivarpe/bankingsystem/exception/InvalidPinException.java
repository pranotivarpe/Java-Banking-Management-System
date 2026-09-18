package com.pranotivarpe.bankingsystem.exception;

public class InvalidPinException extends BankingException {

    public InvalidPinException(Integer accountNumber, int remainingAttempts) {
        super("Incorrect PIN for account " + accountNumber + ". " + remainingAttempts + " attempt(s) remaining.");
    }
}
