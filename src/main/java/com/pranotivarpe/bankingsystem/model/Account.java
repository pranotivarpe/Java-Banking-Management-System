package com.pranotivarpe.bankingsystem.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    private static final int MAX_FAILED_PIN_ATTEMPTS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accountNumber;

    @Enumerated(EnumType.STRING)
    private BankName bank;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    private BigDecimal balance = BigDecimal.ZERO;

    private String pinHash;
    private int failedPinAttempts = 0;
    private boolean locked = false;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();

    protected Account() {
        // required by JPA
    }

    public Account(BankName bank, AccountType accountType, Customer customer, String pinHash) {
        this.bank = bank;
        this.accountType = accountType;
        this.customer = customer;
        this.balance = BigDecimal.ZERO;
        this.pinHash = pinHash;
    }

    public Integer getAccountNumber() {
        return accountNumber;
    }

    public BankName getBank() {
        return bank;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public String getPinHash() {
        return pinHash;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getRemainingPinAttempts() {
        return Math.max(0, MAX_FAILED_PIN_ATTEMPTS - failedPinAttempts);
    }

    public void recordFailedPinAttempt() {
        failedPinAttempts++;
        if (failedPinAttempts >= MAX_FAILED_PIN_ATTEMPTS) {
            locked = true;
        }
    }

    public void recordSuccessfulPinAttempt() {
        failedPinAttempts = 0;
    }
}
