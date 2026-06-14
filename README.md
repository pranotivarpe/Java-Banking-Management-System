# 🏦 Java Banking Management System

![Java](https://img.shields.io/badge/Java-SE-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?style=flat-square&logo=mysql&logoColor=white)
![JDBC](https://img.shields.io/badge/JDBC-Connectivity-007396?style=flat-square)

A **console-based Banking Management System** built in Java with MySQL via JDBC. Supports account creation across multiple banks, deposit/withdrawal transactions, and balance enquiries.

---

## Features

- 🏦 **Multi-Bank Support** — Open accounts with SBI, AXIS, or ICICI
- 👤 **Account Creation** — Collect customer details (name, DOB, address, contact)
- 💳 **Account Types** — Savings and Current accounts
- 💰 **Transactions** — Deposit, Withdraw, and Balance Enquiry
- 📋 **Transaction History** — View previous transaction records
- 🔐 **PIN Security** — Account PIN protection

---

## Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java SE | Core application logic |
| MySQL | Persistent data storage |
| JDBC | Java-to-database connectivity |
| mysql-connector-j 8.0.33 | MySQL JDBC driver |

---

## Project Structure

```
Basic-BMS/
├── BankingSystem.java          # Main application class
├── mysql-connector-j-8.0.33.jar  # MySQL JDBC driver
└── bin/                        # Compiled .class files
```

---

## Getting Started

### Prerequisites

- Java JDK 8+
- MySQL server running
- MySQL Connector JAR (included)

### Database Setup

```sql
CREATE DATABASE banking_system;
USE banking_system;

CREATE TABLE customer (
  AccountNumber INT PRIMARY KEY,
  BankName VARCHAR(50),
  FirstName VARCHAR(100),
  LastName VARCHAR(100),
  DOB DATE,
  ContactNum BIGINT,
  Address VARCHAR(255),
  AccountType VARCHAR(20)
);
```

### Run

```bash
# Compile
javac -cp mysql-connector-j-8.0.33.jar BankingSystem.java

# Run
java -cp .:mysql-connector-j-8.0.33.jar BankingSystem
# Windows:
java -cp .;mysql-connector-j-8.0.33.jar BankingSystem
```

> Update the database credentials (host, user, password) inside `BankingSystem.java` before running.

---

## Menu Options

```
1. Create Account
2. Deposit
3. Withdraw
4. Balance Enquiry
5. Transaction History
6. Exit
```