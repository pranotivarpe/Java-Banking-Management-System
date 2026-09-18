package com.pranotivarpe.bankingsystem;

import com.pranotivarpe.bankingsystem.console.BankingConsoleRunner;
import com.pranotivarpe.bankingsystem.exception.AccountLockedException;
import com.pranotivarpe.bankingsystem.exception.InvalidPinException;
import com.pranotivarpe.bankingsystem.model.Account;
import com.pranotivarpe.bankingsystem.model.AccountType;
import com.pranotivarpe.bankingsystem.model.BankName;
import com.pranotivarpe.bankingsystem.repository.AccountRepository;
import com.pranotivarpe.bankingsystem.service.AccountService;
import com.pranotivarpe.bankingsystem.service.CreateAccountRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Runs the full Spring context against a real, throwaway MySQL instance in Docker (Testcontainers) -
// not H2 - so these tests exercise the exact database engine and SQL dialect the app runs on in
// production, not an in-memory approximation of it.
@SpringBootTest
@Testcontainers
class AccountServiceIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    // BankingConsoleRunner is a CommandLineRunner that blocks on System.in for interactive menu input;
    // Spring Boot invokes every CommandLineRunner bean automatically on startup, so without this the
    // test process would hang waiting for console input that never arrives. Replacing it with a mock
    // makes it a no-op while the rest of the real application context still wires up normally.
    @MockitoBean
    private BankingConsoleRunner consoleRunner;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void createAccountThenDeposit_persistsToRealDatabase() {
        Account created = accountService.createAccount(requestWithPin("1234"));

        accountService.deposit(created.getAccountNumber(), new BigDecimal("500.00"));

        Account reloaded = accountRepository.findById(created.getAccountNumber()).orElseThrow();
        assertThat(reloaded.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    void transfer_isAtomicAcrossTwoRealAccounts() {
        Account from = accountService.createAccount(requestWithPin("1111"));
        accountService.deposit(from.getAccountNumber(), new BigDecimal("1000.00"));
        Account to = accountService.createAccount(requestWithPin("2222"));

        accountService.transfer(from.getAccountNumber(), to.getAccountNumber(), new BigDecimal("400.00"));

        assertThat(accountRepository.findById(from.getAccountNumber()).orElseThrow().getBalance())
                .isEqualByComparingTo("600.00");
        assertThat(accountRepository.findById(to.getAccountNumber()).orElseThrow().getBalance())
                .isEqualByComparingTo("400.00");
    }

    // Regression test for the Phase 3 bug: authenticate() was originally @Transactional, which rolled
    // back the failed-attempt counter save whenever it threw, so lockout never actually persisted. This
    // test proves the fix holds against a real database with real transaction commits, not just mocks.
    @Test
    void authenticate_locksAccountAfterThreeFailedAttempts_persistedAcrossSeparateCalls() {
        Account account = accountService.createAccount(requestWithPin("9999"));

        assertThatThrownBy(() -> accountService.authenticate(account.getAccountNumber(), "0000"))
                .isInstanceOf(InvalidPinException.class);
        assertThatThrownBy(() -> accountService.authenticate(account.getAccountNumber(), "0000"))
                .isInstanceOf(InvalidPinException.class);
        assertThatThrownBy(() -> accountService.authenticate(account.getAccountNumber(), "0000"))
                .isInstanceOf(AccountLockedException.class);

        Account reloaded = accountRepository.findById(account.getAccountNumber()).orElseThrow();
        assertThat(reloaded.isLocked()).isTrue();
    }

    private CreateAccountRequest requestWithPin(String pin) {
        return new CreateAccountRequest(BankName.SBI, "Alice", "Smith",
                LocalDate.of(1990, 1, 1), "Pune", 9999999999L, AccountType.SAVING, pin);
    }
}
