package com.pranotivarpe.bankingsystem.service;

import com.pranotivarpe.bankingsystem.exception.AccountLockedException;
import com.pranotivarpe.bankingsystem.exception.AccountNotFoundException;
import com.pranotivarpe.bankingsystem.exception.InsufficientFundsException;
import com.pranotivarpe.bankingsystem.exception.InvalidAccountDataException;
import com.pranotivarpe.bankingsystem.exception.InvalidAmountException;
import com.pranotivarpe.bankingsystem.exception.InvalidPinException;
import com.pranotivarpe.bankingsystem.exception.MinimumBalanceViolationException;
import com.pranotivarpe.bankingsystem.model.Account;
import com.pranotivarpe.bankingsystem.model.AccountType;
import com.pranotivarpe.bankingsystem.model.Customer;
import com.pranotivarpe.bankingsystem.model.Transaction;
import com.pranotivarpe.bankingsystem.model.TransactionType;
import com.pranotivarpe.bankingsystem.repository.AccountRepository;
import com.pranotivarpe.bankingsystem.repository.CustomerRepository;
import com.pranotivarpe.bankingsystem.repository.TransactionRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private static final int MINIMUM_AGE_YEARS = 18;
    private static final BigDecimal MINIMUM_SAVINGS_BALANCE = new BigDecimal("500.00");

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    public AccountServiceImpl(CustomerRepository customerRepository,
                               AccountRepository accountRepository,
                               TransactionRepository transactionRepository,
                               Validator validator,
                               PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        validateRequest(request);
        Customer customer = new Customer(
                request.firstName(),
                request.lastName(),
                request.dob(),
                request.address(),
                request.contactNum()
        );
        customerRepository.save(customer);

        Account account = new Account(request.bank(), request.accountType(), customer,
                passwordEncoder.encode(request.pin()));
        accountRepository.save(account);

        log.info("Created account {} for customer {} {}", account.getAccountNumber(),
                customer.getFirstName(), customer.getLastName());
        return account;
    }

    // Deliberately NOT @Transactional: getAccount() and accountRepository.save() each run in their
    // own auto-committing transaction (Spring Data's default per-repository-method behavior). If this
    // whole method were wrapped in one @Transactional, throwing the PIN/lockout exception below would
    // roll back the save() that recorded the failed attempt, together with the exception that reports
    // it — silently erasing the very lockout counter this method exists to persist.
    @Override
    public void authenticate(Integer accountNumber, String pin) {
        Account account = getAccount(accountNumber);
        if (account.isLocked()) {
            throw new AccountLockedException(accountNumber);
        }
        if (!passwordEncoder.matches(pin, account.getPinHash())) {
            account.recordFailedPinAttempt();
            accountRepository.save(account);
            if (account.isLocked()) {
                throw new AccountLockedException(accountNumber);
            }
            throw new InvalidPinException(accountNumber, account.getRemainingPinAttempts());
        }
        account.recordSuccessfulPinAttempt();
        accountRepository.save(account);
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
        BigDecimal balanceAfterWithdrawal = account.getBalance().subtract(amount);
        if (account.getAccountType() == AccountType.SAVING
                && balanceAfterWithdrawal.compareTo(MINIMUM_SAVINGS_BALANCE) < 0) {
            throw new MinimumBalanceViolationException(accountNumber, MINIMUM_SAVINGS_BALANCE);
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
        if (amount.scale() > 2) {
            throw new InvalidAmountException("Amount cannot have more than 2 decimal places");
        }
    }

    private void validateRequest(CreateAccountRequest request) {
        Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new InvalidAccountDataException(violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet()));
        }

        int age = Period.between(request.dob(), LocalDate.now()).getYears();
        if (age < MINIMUM_AGE_YEARS) {
            throw new InvalidAccountDataException(
                    "Account holder must be at least " + MINIMUM_AGE_YEARS + " years old");
        }
    }
}
