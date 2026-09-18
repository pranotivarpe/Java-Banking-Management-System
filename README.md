# 🏦 Java Banking Management System

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=flat-square&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=flat-square&logo=apachemaven&logoColor=white)

A **console-based Banking Management System** built on Spring Boot and Spring Data JPA, backed by MySQL. Supports account creation across multiple banks, deposits/withdrawals, transaction history, and profile management.

---

## Features

- 🏦 **Multi-Bank Support** — Open accounts with SBI, AXIS, or ICICI
- 👤 **Account Creation** — Collect customer details (name, DOB, address, contact)
- 💳 **Account Types** — Savings and Current accounts
- 💰 **Transactions** — Deposit and Withdraw, with balance checks
- 📋 **Transaction History** — View past transactions, most recent first
- ✏️ **Profile Management** — Update name, address, or contact number
- ❌ **Account Closure**

---

## Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java 17 | Core application language |
| Spring Boot 3.5 | Dependency injection, application bootstrap |
| Spring Data JPA / Hibernate | ORM — object-relational mapping to MySQL |
| MySQL | Persistent data storage |
| Maven | Build and dependency management |

---

## Architecture

Layered architecture — each package has a single responsibility:

```
com.pranotivarpe.bankingsystem/
├── model/         Customer, Account, Transaction (JPA entities) + enums
├── repository/    Spring Data JPA repositories (no hand-written SQL)
├── service/       Business logic — validation, transactions, exceptions
├── exception/     Custom domain exceptions
└── console/       CommandLineRunner-driven menu, isolated from business logic
```

`Customer` (1) —< `Account` (1) —< `Transaction`: a customer can hold multiple accounts, each with its own transaction history.

---

## Getting Started

### Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL server running locally

### Configure credentials

Credentials are read from environment variables — never hardcoded in source or committed to git.

```bash
export DB_URL="jdbc:mysql://localhost:3306/BankingSystem?createDatabaseIfNotExist=true"
export DB_USER="root"
export DB_PASSWORD="your-password-here"
```

### Run

```bash
mvn spring-boot:run
```

The schema (`customers`, `accounts`, `transactions`) is created automatically on first run via Hibernate.

### Build a runnable jar

```bash
mvn package
java -jar target/banking-management-system-0.1.0.jar
```

---

## Menu Options

```
1. Create Account
2. Make Transaction (Deposit / Withdraw)
3. View Transaction History
4. Modify Personal Information
5. Close Account
6. Exit
```
