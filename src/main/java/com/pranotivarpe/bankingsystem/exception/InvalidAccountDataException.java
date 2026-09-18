package com.pranotivarpe.bankingsystem.exception;

import java.util.Set;
import java.util.stream.Collectors;

public class InvalidAccountDataException extends BankingException {

    public InvalidAccountDataException(Set<String> violationMessages) {
        super(violationMessages.stream().collect(Collectors.joining("; ")));
    }

    public InvalidAccountDataException(String message) {
        super(message);
    }
}
