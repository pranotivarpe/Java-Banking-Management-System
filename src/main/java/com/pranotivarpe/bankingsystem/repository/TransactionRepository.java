package com.pranotivarpe.bankingsystem.repository;

import com.pranotivarpe.bankingsystem.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount_AccountNumberOrderByTransactionDateDesc(Integer accountNumber);
}
