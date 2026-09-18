package com.pranotivarpe.bankingsystem.exception;

public class AccountLockedException extends BankingException {

    public AccountLockedException(Integer accountNumber) {
        super("Account " + accountNumber + " is locked due to too many failed PIN attempts.");
    }
}
