import java.util.Scanner;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BankingSystem {

    static class Account {
        int accountNum;
        String firstname;
        String lastname;
        String dob;
        String address;
        long contactNum;
        String bank;
        String accountType;
        int balance;
        int previousTransaction;
        Connection conn;
        Scanner sc;

        Account(Connection conn) {
            this.conn = conn;
            sc = new Scanner(System.in);
        }

        void createAccount() {
            System.out.println("Which bank do you prefer to open an account with: ");
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - ");
            System.out.println("1. SBI");
            System.out.println("2. AXIS");
            System.out.println("3. ICICI");

            int choiceBank = sc.nextInt();
            if (choiceBank == 1) {
                bank = "SBI";
            } else if (choiceBank == 2) {
                bank = "AXIS";
            } else if (choiceBank == 3) {
                bank = "ICICI";
            } else {
                System.out.println("Invalid choice!");
                return;
            }

            System.out.println("Please Enter Your First Name: ");
            System.out.println("- - - - - - - - - - - - - - - - - ");
            sc.nextLine();
            firstname = sc.nextLine();

            System.out.println("Please Enter Your Last Name: ");
            System.out.println("- - - - - - - - - - - - - - - - -  ");
            lastname = sc.nextLine();

            System.out.println("Please Enter Your Date Of Birth (DD/MM/YYYY): ");
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - ");
            String dobStr = sc.next();

            // Convert date string to the desired format
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            try {
                LocalDate dobDate = LocalDate.parse(dobStr, inputFormatter);
                dob = dobDate.format(outputFormatter);
            } catch (Exception e) {
                System.out.println("Invalid date format!");
                return;
            }

            System.out.println("Please Enter Your Address(City): ");
            System.out.println("- - - - - - - - - - - - - - - - - - ");
            sc.nextLine();
            address = sc.nextLine();

            System.out.println("Please Enter Your Contact Number: ");
            System.out.println("- - - - - - - - - - - - - - - - - - ");
            contactNum = sc.nextLong();

            System.out.println("What Type Of Account Would You Like To Open: ");
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
            System.out.println("1. Saving");
            System.out.println("2. Current");
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
            int choice = sc.nextInt();
            if (choice == 1) {
                accountType = "Saving";
            } else if (choice == 2) {
                accountType = "Current";
            } else {
                System.out.println("Invalid choice!");
                return;
            }

            int accountNumber = generateAccountNumber();

            try {
                // Create a prepared statement to insert account details
                String insertQuery = "INSERT INTO customer (AccountNumber, BankName, FirstName, LastName, DOB, ContactNum, Address, AccountType) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertQuery);
                pstmt.setInt(1, accountNumber);
                pstmt.setString(2, bank);
                pstmt.setString(3, firstname);
                pstmt.setString(4, lastname);
                pstmt.setString(5, dob);
                pstmt.setLong(6, contactNum);
                pstmt.setString(7, address);
                pstmt.setString(8, accountType);

                pstmt.executeUpdate();
                pstmt.close();

                System.out.println("Your account has been Created Successfully");
                System.out.println("- - - - - - - - - - - - - - - - - - - - - - ");
                System.out.println("Check Your Details:");
                System.out.println("- - - - - - - - - - - - - - - - - - - - - - ");
                System.out.println("Account Number: " + accountNumber);
                System.out.println("Bank: " + bank);
                System.out.println("Name: " + firstname + " " + lastname);
                System.out.println("DOB: " + dob);
                System.out.println("Address: " + address);
                System.out.println("Contact Number: " + contactNum);
                System.out.println("Account Type: " + accountType);
                System.out.println();
                System.out.println("- - - - - - - - - - - - - - ");
            } catch (SQLException e) {
                System.out.println("Error creating the account: " + e.getMessage());
            }
            return;
        }

        int generateAccountNumber() {
            int accountNumber = 1000;

            try {
                String query = "SELECT COALESCE(MAX(AccountNumber), 999) AS maxAccountNum FROM customer";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                if (rs.next()) {
                    accountNumber = rs.getInt("maxAccountNum") + 1;
                }

                rs.close();
                stmt.close();
            } catch (SQLException e) {
                System.out.println("Error generating account number: " + e.getMessage());
            }

            return accountNumber;
        }

        void BankTransaction() {
            System.out.println("Please Select a Transaction Type");
            System.out.println("- - - - - - - - - - - - - - - - -");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("- - - - - - - - - - - - - - ");
            int choiceType = sc.nextInt();
            if (choiceType == 1) {
                deposit();
            } else if (choiceType == 2) {
                withdraw();
            } else {
                System.out.println("Invalid choice!");
                return;
            }
        }

        void deposit() {
            System.out.println("- - - - - - - - - - - - - - - -");
            System.out.println("Enter the account number:");
            System.out.println("- - - - - - - - - - - - - - - -");
            int accountNumber = sc.nextInt();
            sc.nextLine(); // Consume newline character

            // Check if the account number exists
            if (!checkAccountExists(accountNumber)) {
                System.out.println("Account does not exist!");
                return;
            }
            balance = getAccountBalance(accountNumber);
            System.out.println("- - - - - - - - - - - - - - - -");
            getAccountDetails(accountNumber); // Retrieve account details
            System.out.println("Enter the amount to deposit:");
            System.out.println("- - - - - - - - - - - - - - - -");
            int amount = sc.nextInt();
            if (amount > 0) {
                updateBalance(accountNumber, amount); // Update the balance in the database
                balance += amount;
                System.out.println("Balance after deposit: " + balance);
                System.out.println("- - - - - - - - - - - - - - ");
                saveTransaction(accountNumber, "Deposit", amount); // Save the transaction
                previousTransaction = amount;
            } else {
                System.out.println("Invalid amount!");
            }
                
        }

        void withdraw() {
            System.out.println("- - - - - - - - - - - - - - - -");
            System.out.println("Enter the account number:");
            System.out.println("- - - - - - - - - - - - - -");
            int accountNumber = sc.nextInt();
            sc.nextLine(); // Consume newline character

            // Check if the account number exists
            if (!checkAccountExists(accountNumber)) {
                System.out.println("Account does not exist!");
                return;
            }

            balance = getAccountBalance(accountNumber);

            System.out.println("- - - - - - - - - - - - - - - -");
            getAccountDetails(accountNumber);
            System.out.println("Enter the amount to withdraw:");
            System.out.println("- - - - - - - - - - - - - - - -");
            int amount = sc.nextInt();
            if (amount > 0) {
                // Check if there is sufficient balance
                if (amount > balance) {
                    System.out.println("Insufficient balance!");
                    return;
                }

                updateBalance(accountNumber, -amount); // Update the balance in the database
                balance -= amount;
                System.out.println("Balance after withdrawal: " + balance);
                System.out.println("- - - - - - - - - - - - - - ");
                saveTransaction(accountNumber, "Withdrawal", amount);
                previousTransaction = -amount;
            } else {
                System.out.println("Invalid amount!");
            }
        }

        void getAccountDetails(int accountNumber) {
            try {
                String query = "SELECT FirstName, LastName, Balance FROM customer WHERE AccountNumber = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, accountNumber);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String firstName = rs.getString("FirstName");
                    String lastName = rs.getString("LastName");
                    int Balance = rs.getInt("Balance");

                    // Print the account holder's name and balance
                    System.out.println("Name: " + firstName + " " + lastName);
                    System.out.println("Balance: " + Balance);
                } else {
                    System.out.println("Account does not exist!");
                }

                rs.close();
                pstmt.close();
            } catch (SQLException e) {
                System.out.println("Error retrieving account details: " + e.getMessage());
            }
        }

        int getAccountBalance(int accountNumber) {
            int balance = 0;

            try {
                String query = "SELECT Balance FROM customer WHERE AccountNumber = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, accountNumber);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    balance = rs.getInt("Balance");
                }

                rs.close();
                pstmt.close();
            } catch (SQLException e) {
                System.out.println("Error retrieving account balance: " + e.getMessage());
            }

            return balance;
        }

        boolean checkAccountExists(int accountNumber) {
            try {
                String query = "SELECT * FROM customer WHERE AccountNumber = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, accountNumber);
                ResultSet rs = pstmt.executeQuery();
                boolean exists = rs.next();
                rs.close();
                pstmt.close();
                return exists;
            } catch (SQLException e) {
                System.out.println("Error checking account existence: " + e.getMessage());
                return false;
            }
        }

        void updateBalance(int accountNumber, int amount) {
            try {
                String updateQuery = "UPDATE customer SET Balance = Balance + ? WHERE AccountNumber = ?";
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setInt(1, amount);
                pstmt.setInt(2, accountNumber);
                pstmt.executeUpdate();
                pstmt.close();
            } catch (SQLException e) {
                System.out.println("Error updating balance: " + e.getMessage());
            }
        }

        void saveTransaction(int accountNumber, String transactionType, int amount) {
            try {
                String insertQuery = "INSERT INTO transaction (AccountNumber, TransactionType, Amount) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertQuery);
                pstmt.setInt(1, accountNumber);
                pstmt.setString(2, transactionType);
                pstmt.setInt(3, amount);
                pstmt.executeUpdate();
                pstmt.close();
            } catch (SQLException e) {
                System.out.println("Error saving transaction: " + e.getMessage());
            }
        }

        void viewTransactionHistory() {
            try {
                System.out.println("Enter the account number:");
                System.out.println("------------------------");
                int accountNumber = sc.nextInt();
                sc.nextLine(); // Consume newline character
        
                // Check if the account number exists
                if (!checkAccountExists(accountNumber)) {
                    System.out.println("Account does not exist!");
                    return;
                }
        
                String query = "SELECT * FROM transaction WHERE AccountNumber = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, accountNumber);
                ResultSet rs = pstmt.executeQuery();
        
                System.out.println("Transaction History for Account Number: " + accountNumber);
                System.out.println("-----------------------------------------------------");
        
                boolean hasTransactions = false;
                while (rs.next()) {
                    int transactionId = rs.getInt("TransactionId");
                    String transactionType = rs.getString("TransactionType");
                    int amount = rs.getInt("Amount");
                    Timestamp transactionDate = rs.getTimestamp("TransactionDate");
        
                    System.out.println("Transaction ID: " + transactionId);
                    System.out.println("Transaction Type: " + transactionType);
                    System.out.println("Amount: " + amount);
                    System.out.println("Date: " + transactionDate);
                    System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
        
                    hasTransactions = true;
                }
        
                if (!hasTransactions) {
                    System.out.println("No transaction history found for the account");
                    System.out.println("- - - - - - - - - - - - - - ");
                }
        
                rs.close();
                pstmt.close();
            } catch (SQLException e) {
                System.out.println("Error retrieving transaction history: " + e.getMessage());
            }
        }        

        void updatePersonalInfo() {
            System.out.println("Please enter your account number:");
            int accountNum = sc.nextInt();
            sc.nextLine(); // Consume newline character

            try {
                String updateQuery;
                PreparedStatement pstmt;
           
                System.out.println("Please select the detail you want to modify:");
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
            System.out.println("1. First Name");
            System.out.println("2. Last Name");
            System.out.println("3. Address");
            System.out.println("4. Contact Number");

            int choice = sc.nextInt();
            sc.nextLine(); // Consume newline character

           

                switch (choice) {
                    case 1:
                        System.out.println("Enter the new First Name:");
                        String newFirstName = sc.nextLine();

                        updateQuery = "UPDATE customer SET FirstName = ? WHERE AccountNumber = ?";
                        pstmt = conn.prepareStatement(updateQuery);
                        pstmt.setString(1, newFirstName);
                        pstmt.setInt(2, accountNum);
                        pstmt.executeUpdate();
                        pstmt.close();

                        System.out.println("First Name updated successfully!");
                        System.out.println("- - - - - - - - - - - - - - - - - ");
                        break;

                    case 2:
                        System.out.println("Enter the new Last Name:");
                        String newLastName = sc.nextLine();

                        updateQuery = "UPDATE customer SET LastName = ? WHERE AccountNumber = ?";
                        pstmt = conn.prepareStatement(updateQuery);
                        pstmt.setString(1, newLastName);
                        pstmt.setInt(2, accountNum);
                        pstmt.executeUpdate();
                        pstmt.close();

                        System.out.println("Last Name updated successfully!");
                        System.out.println("- - - - - - - - - - - - - - - - - ");
                        break;

                    case 3:
                        System.out.println("Enter the new address (City):");
                        String newAddress = sc.nextLine();

                        updateQuery = "UPDATE customer SET Address = ? WHERE AccountNumber = ?";
                        pstmt = conn.prepareStatement(updateQuery);
                        pstmt.setString(1, newAddress);
                        pstmt.setInt(2, accountNum);
                        pstmt.executeUpdate();
                        pstmt.close();

                        System.out.println("Address updated successfully!");
                        System.out.println("- - - - - - - - - - - - - - ");
                        break;

                    case 4:
                        System.out.println("Enter the new contact number:");
                        long newContactNum = sc.nextLong();

                        updateQuery = "UPDATE customer SET ContactNum = ? WHERE AccountNumber = ?";
                        pstmt = conn.prepareStatement(updateQuery);
                        pstmt.setLong(1, newContactNum);
                        pstmt.setInt(2, accountNum);
                        pstmt.executeUpdate();
                        pstmt.close();

                        System.out.println("Contact number updated successfully!");
                        System.out.println("- - - - - - - - - - - - - - ");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                        break;
                }

            } catch (SQLException e) {
                System.out.println("Error modifying personal details: " + e.getMessage());
            }
        }

        void closeAccount() {
            System.out.println("Please enter your account number:");
    int accountNum = sc.nextInt();
    sc.nextLine(); // Consume newline character
            try {
                String deleteQuery = "DELETE FROM transaction WHERE AccountNumber = ?";
                PreparedStatement pstmt2 = conn.prepareStatement(deleteQuery);
                pstmt2.setInt(1, accountNum);
                pstmt2.executeUpdate();
                pstmt2.close();
                
                String updateQuery = "DELETE FROM customer WHERE AccountNumber = ?";
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setInt(1, accountNum);
                pstmt.executeUpdate();
                pstmt.close();
        
                

                System.out.println("Account closed successfully!");
                System.out.println("- - - - - - - - - - - - - - ");

            } catch (SQLException e) {
                System.out.println("Error closing the account: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        // Database credentials are read from environment variables, never hardcoded.
        // Set DB_URL, DB_USER and DB_PASSWORD before running (see README.md).
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/BankingSystem");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (username == null || password == null) {
            System.out.println("Missing database credentials.");
            System.out.println("Please set the DB_USER and DB_PASSWORD environment variables before running.");
            System.out.println("See README.md for setup instructions.");
            return;
        }

        // Establish the database connection
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
            System.out.println("*********  Welcome to National Banking System  *********");
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
            System.out.println();

            Scanner sc = new Scanner(System.in);
            Account customer = new Account(conn);

            int choiceBank = 0;
            while (choiceBank != 6) {
                System.out.println("1. Create Account");
                System.out.println("2. Make Transaction");
                System.out.println("3. View Transaction History");
                System.out.println("4. Modify Personal Information");
                System.out.println("5. Close Account");
                System.out.println("6. Exit");

                choiceBank = sc.nextInt();
                sc.nextLine();

                switch (choiceBank) {
                    case 1:
                        customer.createAccount();
                        break;

                    case 2:
                        customer.BankTransaction();
                        break;

                    case 3:
                        customer.viewTransactionHistory();
                        break;

                    case 4:
                        customer.updatePersonalInfo();
                        break;

                    case 5:
                        customer.closeAccount();
                        break;

                    case 6:
                        System.out.println("Thank you for using our services :)");
                        break;

                    default:
                        System.out.println("Invalid choice.");
                        break;
                }
            }
            sc.close();

        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        }
    }
}
