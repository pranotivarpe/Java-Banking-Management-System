package com.pranotivarpe.bankingsystem.exception;

// Common base so the console layer can catch one type instead of an ever-growing multi-catch list.
public abstract class BankingException extends RuntimeException {

    protected BankingException(String message) {
        super(message);
    }
}
