# 🏦 Java Banking Management System

[![CI](https://github.com/pranotivarpe/Java-Banking-Management-System/actions/workflows/ci.yml/badge.svg)](https://github.com/pranotivarpe/Java-Banking-Management-System/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
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
- 🔐 **PIN Security** — BCrypt-hashed 4-digit PIN required for every account operation, with account lockout after 3 failed attempts
- 🔁 **Fund Transfers** — Move money between accounts atomically, with minimum-balance rules enforced
- 📈 **Savings Interest** — Monthly interest credited to savings accounts, both on demand and via a scheduled job
- 🛠️ **Admin Console** — View all accounts, search by customer last name, trigger interest runs

---

## Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java 17 | Core application language |
| Spring Boot 3.5 | Dependency injection, application bootstrap |
| Spring Data JPA / Hibernate | ORM — object-relational mapping to MySQL |
| Spring Security Crypto | BCrypt password hashing for account PINs |
| MySQL | Persistent data storage |
| Maven | Build and dependency management |
| Docker / Docker Compose | Containerized app + database, one-command local demo |
| GitHub Actions | CI — builds and runs the full test suite on every push |

---

## Architecture

Layered architecture — each package has a single responsibility:

```
com.pranotivarpe.bankingsystem/
├── model/         Customer, Account, Transaction (JPA entities) + enums
├── repository/    Spring Data JPA repositories (no hand-written SQL)
├── service/       Business logic — validation, transactions, exceptions
├── security/      BCrypt PasswordEncoder configuration
├── scheduler/     @Scheduled monthly interest job
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

## Run with Docker

No local Java, Maven, or MySQL installation needed — just Docker.

```bash
# Start MySQL in the background
docker compose up -d mysql

# Run the console app, attached to your terminal
docker compose run --rm app
```

> Use `docker compose run`, not `docker compose up`, for the `app` service: `up` doesn't attach an
> interactive terminal to a specific service in a multi-service stack, and this app needs one to read your
> menu input. `run --rm` attaches your terminal and removes the container when you exit.

The app image is a multi-stage build (`Dockerfile`): a `maven` build stage compiles the jar, and a slim
`eclipse-temurin:17-jre` stage runs it — no build tools or source ship in the final image.

To stop everything and remove the database volume:

```bash
docker compose down -v
```

---

## Running Tests

```bash
mvn test
```

- **Unit tests** (`AccountServiceImplTest`) mock the repository layer with Mockito — no database needed.
- **Integration tests** (`AccountServiceIntegrationTest`) use [Testcontainers](https://testcontainers.com/) to run
  the full Spring context against a real, throwaway MySQL container via Docker — not an in-memory substitute.

> **Docker on macOS via Colima:** Testcontainers' Ryuk cleanup sidecar can fail to start under Colima's Docker
> socket. If integration tests fail with a `ContainerLaunchException` for `testcontainers/ryuk`, run with
> `TESTCONTAINERS_RYUK_DISABLED=true mvn test` (containers still stop on a normal JVM shutdown; this only disables
> the crash-safety net). Not needed on Docker Desktop or in CI.

The [CI workflow](.github/workflows/ci.yml) runs this exact same `mvn verify` — including the Testcontainers
integration tests — on every push and pull request, on GitHub-hosted runners that have Docker built in.

---

## Menu Options

```
1. Create Account
2. Make Transaction (Deposit / Withdraw)
3. Transfer Funds
4. View Transaction History
5. Modify Personal Information
6. Close Account
7. Admin Menu (view all / search / apply interest)
8. Exit
```
