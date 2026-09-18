package com.pranotivarpe.bankingsystem.service;

import com.pranotivarpe.bankingsystem.model.Account;
import com.pranotivarpe.bankingsystem.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    Account createAccount(CreateAccountRequest request);

    void authenticate(Integer accountNumber, String pin);

    Account deposit(Integer accountNumber, BigDecimal amount);

    Account withdraw(Integer accountNumber, BigDecimal amount);

    Account getAccount(Integer accountNumber);

    List<Transaction> getTransactionHistory(Integer accountNumber);

    Account updateFirstName(Integer accountNumber, String firstName);

    Account updateLastName(Integer accountNumber, String lastName);

    Account updateAddress(Integer accountNumber, String address);

    Account updateContactNumber(Integer accountNumber, Long contactNumber);

    void closeAccount(Integer accountNumber);
}
