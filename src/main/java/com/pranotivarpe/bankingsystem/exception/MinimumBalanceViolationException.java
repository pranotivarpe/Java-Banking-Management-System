package com.pranotivarpe.bankingsystem.exception;

import java.math.BigDecimal;

public class MinimumBalanceViolationException extends BankingException {

    public MinimumBalanceViolationException(Integer accountNumber, BigDecimal minimumBalance) {
        super("Withdrawal declined: savings account " + accountNumber
                + " must keep a minimum balance of " + minimumBalance);
    }
}
