package com.pranotivarpe.bankingsystem.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Integer accountNumber) {
        super("No account found with account number: " + accountNumber);
    }
}
