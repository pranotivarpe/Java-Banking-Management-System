package com.pranotivarpe.bankingsystem.service;

import com.pranotivarpe.bankingsystem.model.AccountType;
import com.pranotivarpe.bankingsystem.model.BankName;

import java.time.LocalDate;

public record CreateAccountRequest(
        BankName bank,
        String firstName,
        String lastName,
        LocalDate dob,
        String address,
        Long contactNum,
        AccountType accountType
) {
}
