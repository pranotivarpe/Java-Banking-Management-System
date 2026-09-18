package com.pranotivarpe.bankingsystem.exception;

public class AccountNotFoundException extends BankingException {

    public AccountNotFoundException(Integer accountNumber) {
        super("No account found with account number: " + accountNumber);
    }
}
