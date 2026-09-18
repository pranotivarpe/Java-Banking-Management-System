package com.pranotivarpe.bankingsystem.service;

import com.pranotivarpe.bankingsystem.exception.AccountLockedException;
import com.pranotivarpe.bankingsystem.exception.InsufficientFundsException;
import com.pranotivarpe.bankingsystem.exception.InvalidAccountDataException;
import com.pranotivarpe.bankingsystem.exception.InvalidAmountException;
import com.pranotivarpe.bankingsystem.exception.InvalidPinException;
import com.pranotivarpe.bankingsystem.exception.MinimumBalanceViolationException;
import com.pranotivarpe.bankingsystem.model.Account;
import com.pranotivarpe.bankingsystem.model.AccountType;
import com.pranotivarpe.bankingsystem.model.BankName;
import com.pranotivarpe.bankingsystem.model.Customer;
import com.pranotivarpe.bankingsystem.repository.AccountRepository;
import com.pranotivarpe.bankingsystem.repository.CustomerRepository;
import com.pranotivarpe.bankingsystem.repository.TransactionRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Mocked repositories -> no database needed. The real Hibernate Validator is used (not mocked) so
// the Bean Validation rules on CreateAccountRequest are actually exercised, not just assumed correct.
@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        accountService = new AccountServiceImpl(
                customerRepository, accountRepository, transactionRepository, validator, passwordEncoder);
    }

    // --- createAccount ---

    @Test
    void createAccount_savesCustomerAndAccount_whenRequestIsValid() {
        when(passwordEncoder.encode("1234")).thenReturn("hashed-pin");

        Account account = accountService.createAccount(validRequest());

        verify(customerRepository).save(any(Customer.class));
        verify(accountRepository).save(any(Account.class));
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.getPinHash()).isEqualTo("hashed-pin");
    }

    @Test
    void createAccount_throws_whenApplicantIsUnderage() {
        CreateAccountRequest request = new CreateAccountRequest(BankName.SBI, "Minor", "Kid",
                LocalDate.now().minusYears(10), "Pune", 9999999999L, AccountType.SAVING, "1234");

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(InvalidAccountDataException.class)
                .hasMessageContaining("at least 18");
        verifyNoInteractions(customerRepository, accountRepository);
    }

    @Test
    void createAccount_throws_whenFirstNameBlank() {
        CreateAccountRequest request = new CreateAccountRequest(BankName.SBI, "", "Kid",
                LocalDate.of(1990, 1, 1), "Pune", 9999999999L, AccountType.SAVING, "1234");

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(InvalidAccountDataException.class);
        verifyNoInteractions(customerRepository, accountRepository);
    }

    @Test
    void createAccount_throws_whenPinIsNotFourDigits() {
        CreateAccountRequest request = new CreateAccountRequest(BankName.SBI, "Alice", "Smith",
                LocalDate.of(1990, 1, 1), "Pune", 9999999999L, AccountType.SAVING, "12");

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(InvalidAccountDataException.class);
    }

    // --- deposit / withdraw ---

    @Test
    void deposit_increasesBalance_andRecordsTransaction() {
        Account account = existingAccount(AccountType.CURRENT, "1000.00");
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        Account result = accountService.deposit(1, new BigDecimal("500.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("1500.00");
        verify(transactionRepository).save(any());
    }

    @Test
    void deposit_throws_whenAmountIsZeroOrNegative() {
        assertThatThrownBy(() -> accountService.deposit(1, BigDecimal.ZERO))
                .isInstanceOf(InvalidAmountException.class);
        verifyNoInteractions(accountRepository);
    }

    @Test
    void withdraw_throws_whenBalanceInsufficient() {
        Account account = existingAccount(AccountType.CURRENT, "100.00");
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw(1, new BigDecimal("200.00")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void withdraw_throws_whenSavingsWithdrawalBreachesMinimumBalance() {
        Account account = existingAccount(AccountType.SAVING, "1000.00");
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw(1, new BigDecimal("600.00")))
                .isInstanceOf(MinimumBalanceViolationException.class);
    }

    @Test
    void withdraw_allowsCurrentAccountToGoBelowSavingsMinimum() {
        Account account = existingAccount(AccountType.CURRENT, "1000.00");
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        Account result = accountService.withdraw(1, new BigDecimal("900.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("100.00");
    }

    // --- authenticate ---

    @Test
    void authenticate_resetsFailedAttempts_onCorrectPin() {
        Account account = existingAccount(AccountType.SAVING, "0.00");
        account.recordFailedPinAttempt();
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("1234", account.getPinHash())).thenReturn(true);

        accountService.authenticate(1, "1234");

        assertThat(account.getRemainingPinAttempts()).isEqualTo(3);
    }

    @Test
    void authenticate_locksAccount_afterThreeFailedAttempts() {
        Account account = existingAccount(AccountType.SAVING, "0.00");
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", account.getPinHash())).thenReturn(false);

        assertThatThrownBy(() -> accountService.authenticate(1, "wrong")).isInstanceOf(InvalidPinException.class);
        assertThatThrownBy(() -> accountService.authenticate(1, "wrong")).isInstanceOf(InvalidPinException.class);
        assertThatThrownBy(() -> accountService.authenticate(1, "wrong")).isInstanceOf(AccountLockedException.class);

        assertThat(account.isLocked()).isTrue();
    }

    @Test
    void authenticate_rejectsImmediately_whenAlreadyLocked() {
        Account account = existingAccount(AccountType.SAVING, "0.00");
        account.recordFailedPinAttempt();
        account.recordFailedPinAttempt();
        account.recordFailedPinAttempt();
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.authenticate(1, "1234"))
                .isInstanceOf(AccountLockedException.class);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    // --- transfer ---

    @Test
    void transfer_movesMoneyBetweenAccounts() {
        Account from = existingAccount(AccountType.CURRENT, "1000.00");
        Account to = existingAccount(AccountType.CURRENT, "500.00");
        when(accountRepository.findById(1)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2)).thenReturn(Optional.of(to));

        accountService.transfer(1, 2, new BigDecimal("300.00"));

        assertThat(from.getBalance()).isEqualByComparingTo("700.00");
        assertThat(to.getBalance()).isEqualByComparingTo("800.00");
        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    void transfer_throws_whenSameAccount() {
        assertThatThrownBy(() -> accountService.transfer(1, 1, BigDecimal.TEN))
                .isInstanceOf(InvalidAmountException.class);
        verifyNoInteractions(accountRepository);
    }

    // --- interest ---

    @Test
    void applyMonthlyInterest_creditsOnlySavingsAccountsWithPositiveBalance() {
        Account savings = existingAccount(AccountType.SAVING, "3000.00");
        Account zeroBalanceSavings = existingAccount(AccountType.SAVING, "0.00");
        when(accountRepository.findByAccountType(AccountType.SAVING))
                .thenReturn(List.of(savings, zeroBalanceSavings));

        InterestApplicationResult result = accountService.applyMonthlyInterestToSavingsAccounts();

        // 3000 * 4% annual / 12 months = 10.00
        assertThat(result.accountsCredited()).isEqualTo(1);
        assertThat(result.totalInterestPaid()).isEqualByComparingTo("10.00");
        assertThat(savings.getBalance()).isEqualByComparingTo("3010.00");
        assertThat(zeroBalanceSavings.getBalance()).isEqualByComparingTo("0.00");
    }

    private CreateAccountRequest validRequest() {
        return new CreateAccountRequest(BankName.SBI, "Alice", "Smith",
                LocalDate.of(1990, 1, 1), "Pune", 9999999999L, AccountType.SAVING, "1234");
    }

    private Account existingAccount(AccountType type, String balance) {
        Customer customer = new Customer("Test", "User", LocalDate.of(1990, 1, 1), "City", 9999999999L);
        Account account = new Account(BankName.SBI, type, customer, "hashed-pin");
        account.credit(new BigDecimal(balance));
        return account;
    }
}
