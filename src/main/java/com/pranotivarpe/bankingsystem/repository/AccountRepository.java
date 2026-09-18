package com.pranotivarpe.bankingsystem.repository;

import com.pranotivarpe.bankingsystem.model.Account;
import com.pranotivarpe.bankingsystem.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    List<Account> findByAccountType(AccountType accountType);

    List<Account> findByCustomer_LastNameContainingIgnoreCase(String lastName);
}
