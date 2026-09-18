package com.pranotivarpe.bankingsystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_number")
    private Account account;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private BigDecimal amount;

    // Only set for TRANSFER_OUT/TRANSFER_IN: the account on the other side of the transfer.
    private Integer counterpartyAccountNumber;

    @CreationTimestamp
    private LocalDateTime transactionDate;

    protected Transaction() {
        // required by JPA
    }

    public Transaction(Account account, TransactionType transactionType, BigDecimal amount) {
        this(account, transactionType, amount, null);
    }

    public Transaction(Account account, TransactionType transactionType, BigDecimal amount,
                        Integer counterpartyAccountNumber) {
        this.account = account;
        this.transactionType = transactionType;
        this.amount = amount;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public Integer getCounterpartyAccountNumber() {
        return counterpartyAccountNumber;
    }
}
