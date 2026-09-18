package com.pranotivarpe.bankingsystem.service;

import com.pranotivarpe.bankingsystem.exception.AccountNotFoundException;
import com.pranotivarpe.bankingsystem.exception.InsufficientFundsException;
import com.pranotivarpe.bankingsystem.exception.InvalidAmountException;
import com.pranotivarpe.bankingsystem.model.Account;
import com.pranotivarpe.bankingsystem.model.Customer;
import com.pranotivarpe.bankingsystem.model.Transaction;
import com.pranotivarpe.bankingsystem.model.TransactionType;
import com.pranotivarpe.bankingsystem.repository.AccountRepository;
import com.pranotivarpe.bankingsystem.repository.CustomerRepository;
import com.pranotivarpe.bankingsystem.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountServiceImpl(CustomerRepository customerRepository,
                               AccountRepository accountRepository,
                               TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        Customer customer = new Customer(
                request.firstName(),
                request.lastName(),
                request.dob(),
                request.address(),
                request.contactNum()
        );
        customerRepository.save(customer);

        Account account = new Account(request.bank(), request.accountType(), customer);
        accountRepository.save(account);

        log.info("Created account {} for customer {} {}", account.getAccountNumber(),
                customer.getFirstName(), customer.getLastName());
        return account;
    }

    @Override
    @Transactional
    public Account deposit(Integer accountNumber, BigDecimal amount) {
        validateAmount(amount);
        Account account = getAccount(accountNumber);
        account.credit(amount);
        transactionRepository.save(new Transaction(account, TransactionType.DEPOSIT, amount));
        log.info("Deposited {} into account {}. New balance: {}", amount, accountNumber, account.getBalance());
        return account;
    }

    @Override
    @Transactional
    public Account withdraw(Integer accountNumber, BigDecimal amount) {
        validateAmount(amount);
        Account account = getAccount(accountNumber);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountNumber, account.getBalance(), amount);
        }
        account.debit(amount);
        transactionRepository.save(new Transaction(account, TransactionType.WITHDRAWAL, amount));
        log.info("Withdrew {} from account {}. New balance: {}", amount, accountNumber, account.getBalance());
        return account;
    }

    @Override
    public Account getAccount(Integer accountNumber) {
        return accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    @Override
    public List<Transaction> getTransactionHistory(Integer accountNumber) {
        getAccount(accountNumber); // ensures the account exists before querying history
        return transactionRepository.findByAccount_AccountNumberOrderByTransactionDateDesc(accountNumber);
    }

    @Override
    @Transactional
    public Account updateFirstName(Integer accountNumber, String firstName) {
        Account account = getAccount(accountNumber);
        account.getCustomer().setFirstName(firstName);
        log.info("Updated first name for account {}", accountNumber);
        return account;
    }

    @Override
    @Transactional
    public Account updateLastName(Integer accountNumber, String lastName) {
        Account account = getAccount(accountNumber);
        account.getCustomer().setLastName(lastName);
        log.info("Updated last name for account {}", accountNumber);
        return account;
    }

    @Override
    @Transactional
    public Account updateAddress(Integer accountNumber, String address) {
        Account account = getAccount(accountNumber);
        account.getCustomer().setAddress(address);
        log.info("Updated address for account {}", accountNumber);
        return account;
    }

    @Override
    @Transactional
    public Account updateContactNumber(Integer accountNumber, Long contactNumber) {
        Account account = getAccount(accountNumber);
        account.getCustomer().setContactNum(contactNumber);
        log.info("Updated contact number for account {}", accountNumber);
        return account;
    }

    @Override
    @Transactional
    public void closeAccount(Integer accountNumber) {
        Account account = getAccount(accountNumber);
        accountRepository.delete(account);
        log.info("Closed account {}", accountNumber);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }
}
