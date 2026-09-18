package com.pranotivarpe.bankingsystem.console;

import com.pranotivarpe.bankingsystem.exception.BankingException;
import com.pranotivarpe.bankingsystem.exception.InvalidAmountException;
import com.pranotivarpe.bankingsystem.model.Account;
import com.pranotivarpe.bankingsystem.model.AccountType;
import com.pranotivarpe.bankingsystem.model.BankName;
import com.pranotivarpe.bankingsystem.model.Transaction;
import com.pranotivarpe.bankingsystem.service.AccountService;
import com.pranotivarpe.bankingsystem.service.CreateAccountRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

@Component
public class BankingConsoleRunner implements CommandLineRunner {

    private static final DateTimeFormatter DOB_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AccountService accountService;
    private final Scanner sc = new Scanner(System.in);

    public BankingConsoleRunner(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void run(String... args) {
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
        System.out.println("*********  Welcome to National Banking System  *********");
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - ");

        int choice = 0;
        while (choice != 6) {
            printMenu();
            choice = readInt("Choose an option: ");

            try {
                switch (choice) {
                    case 1 -> createAccount();
                    case 2 -> makeTransaction();
                    case 3 -> viewTransactionHistory();
                    case 4 -> updatePersonalInfo();
                    case 5 -> closeAccount();
                    case 6 -> System.out.println("Thank you for using our services :)");
                    default -> System.out.println("Invalid choice.");
                }
            } catch (BankingException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Create Account");
        System.out.println("2. Make Transaction");
        System.out.println("3. View Transaction History");
        System.out.println("4. Modify Personal Information");
        System.out.println("5. Close Account");
        System.out.println("6. Exit");
    }

    private void createAccount() {
        System.out.println("Which bank do you prefer to open an account with:");
        System.out.println("1. SBI  2. AXIS  3. ICICI");
        BankName bank = switch (readInt("Choice: ")) {
            case 1 -> BankName.SBI;
            case 2 -> BankName.AXIS;
            case 3 -> BankName.ICICI;
            default -> throw new InvalidAmountException("Invalid bank choice!");
        };

        String firstName = readLine("First name: ");
        String lastName = readLine("Last name: ");
        LocalDate dob = readDob("Date of birth (DD/MM/YYYY): ");
        String address = readLine("Address (City): ");
        Long contactNum = readLong("Contact number: ");

        System.out.println("Account type: 1. Saving  2. Current");
        AccountType accountType = switch (readInt("Choice: ")) {
            case 1 -> AccountType.SAVING;
            case 2 -> AccountType.CURRENT;
            default -> throw new InvalidAmountException("Invalid account type choice!");
        };

        String pin = readNewPin();

        Account account = accountService.createAccount(
                new CreateAccountRequest(bank, firstName, lastName, dob, address, contactNum, accountType, pin));

        System.out.println("Account created successfully!");
        printAccountSummary(account);
    }

    private void makeTransaction() {
        System.out.println("1. Deposit  2. Withdraw");
        int choice = readInt("Choice: ");
        int accountNumber = readInt("Account number: ");
        accountService.authenticate(accountNumber, readPin());
        BigDecimal amount = readAmount("Amount: ");

        Account account = switch (choice) {
            case 1 -> accountService.deposit(accountNumber, amount);
            case 2 -> accountService.withdraw(accountNumber, amount);
            default -> throw new InvalidAmountException("Invalid transaction choice!");
        };

        System.out.println("New balance: " + account.getBalance());
    }

    private void viewTransactionHistory() {
        int accountNumber = readInt("Account number: ");
        accountService.authenticate(accountNumber, readPin());
        List<Transaction> history = accountService.getTransactionHistory(accountNumber);

        if (history.isEmpty()) {
            System.out.println("No transaction history found for this account.");
            return;
        }

        System.out.println("Transaction History for Account " + accountNumber);
        System.out.println("-----------------------------------------------");
        for (Transaction t : history) {
            System.out.printf("[%s] %s: %s at %s%n",
                    t.getId(), t.getTransactionType(), t.getAmount(), t.getTransactionDate());
        }
    }

    private void updatePersonalInfo() {
        int accountNumber = readInt("Account number: ");
        accountService.authenticate(accountNumber, readPin());
        System.out.println("1. First Name  2. Last Name  3. Address  4. Contact Number");
        int choice = readInt("Choice: ");

        switch (choice) {
            case 1 -> accountService.updateFirstName(accountNumber, readLine("New first name: "));
            case 2 -> accountService.updateLastName(accountNumber, readLine("New last name: "));
            case 3 -> accountService.updateAddress(accountNumber, readLine("New address: "));
            case 4 -> accountService.updateContactNumber(accountNumber, readLong("New contact number: "));
            default -> System.out.println("Invalid choice!");
        }
        System.out.println("Updated successfully!");
    }

    private void closeAccount() {
        int accountNumber = readInt("Account number: ");
        accountService.authenticate(accountNumber, readPin());
        accountService.closeAccount(accountNumber);
        System.out.println("Account closed successfully!");
    }

    private void printAccountSummary(Account account) {
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Bank: " + account.getBank());
        System.out.println("Name: " + account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
        System.out.println("Account Type: " + account.getAccountType());
        System.out.println("Balance: " + account.getBalance());
    }

    // --- input helpers: retry on bad input instead of crashing ---

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private Long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private BigDecimal readAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private String readPin() {
        return readLine("PIN: ");
    }

    private String readNewPin() {
        while (true) {
            String pin = readLine("Choose a 4-digit PIN: ");
            if (!pin.matches("\\d{4}")) {
                System.out.println("PIN must be exactly 4 digits.");
                continue;
            }
            String confirm = readLine("Confirm PIN: ");
            if (!pin.equals(confirm)) {
                System.out.println("PINs do not match, try again.");
                continue;
            }
            return pin;
        }
    }

    private LocalDate readDob(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return LocalDate.parse(input, DOB_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Please enter the date as DD/MM/YYYY.");
            }
        }
    }
}
