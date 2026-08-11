# Java CLI Expense Tracker

A modular, architecture-focused Java command-line application for tracking personal expenses. Built to demonstrate clean object-oriented design, loose coupling, the Data Access Object (DAO) pattern, and database persistence using JDBC and MariaDB.

---

## 🏛️ Key Architectural Highlights

* **Loose Coupling via Interfaces:** Business logic interacts solely with an `ExpenseRepository` abstraction. Storage backends can be swapped seamlessly without modifying core domain logic.
* **Dependency Injection (DI):** Hand-rolled constructor injection decoupling `ExpenseManager` from concrete storage instantiation.
* **Pluggable Storage Implementations:**
  * `DatabaseExpenseRepository`: SQL persistence via MariaDB and JDBC.
  * `CsvExpenseRepository`: File-based storage reading and writing with standard CSV format.
  * `InMemoryExpenseRepository`: Zero-disk-access RAM storage designed for high-speed unit testing.
* **Granular SQL Operations:** Utilizes MariaDB `UPSERT` (`ON DUPLICATE KEY UPDATE`) and targeted `DELETE` operations for $O(1)$ updates instead of full table overwrites.
* **SQL Injection Prevention:** All SQL persistence uses paramterized `PreparedStatement` queries.
* **Environment Variable Credentials:** Security sensitive parameters (like database credentials) are externalized via environment variables.

---

## 🛠️ Tech Stack & Requirements

* **Language:** Java 11+ (Core Java / JDBC)
* **Database:** MariaDB / MySQL
* **JDBC Driver:** MariaDB Java Client (`mariadb-java-client`)
* **Environment:** Linux, Windows, macOS

---

## 📁 Project Structure
```text
├── Expense.java                   # Core Domain Model
├── ExpenseRepository.java         # DAO Interface (Contract)
├── ExpenseManager.java            # Business Logic Layer
├── CsvExpenseRepository.java      # CSV Storage Implementation
├── InMemoryExpenseRepository.java # In-Memory RAM Storage Implementation
├── DatabaseExpenseRepository.java # JDBC / MariaDB Storage Implementation
├── Main.java                      # Application Entry Point & DI Assembler
├── schema.sql                     # MariaDB Database Bootstrap Script
├── .gitignore
└── README.md
```

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone [https://github.com/felipeaugdev/expense-tracker.git](https://github.com/felipeaugdev/expense-tracker.git)
cd expense-tracker
```

### 2. Database Bootstrap
Initialize the database and schema using the provided `schema.sql` script:

```bash
mariadb -u root -p < schema.sql
```

This creates the `expense_tracker` database and the `expenses` table schema:

```sql
CREATE TABLE IF NOT EXISTS expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(10, 2) NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL
);
```

### 3. Configure Database User & Privileges
Log into MariaDB as root and create a dedicated database user:

```sql
CREATE USER 'expense_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'localhost';
FLUSH PRIVILEGES;
```

### 4. Run the Application
Set your database password in your terminal environment and compile/run the app:

```bash
# Set your environment variables (optional, defaults to local dev settings)
export DB_PASS="your_password"

# Compile all Java files
javac *.java

# Run the Application
java Main
```

---

## 🔄 Switching Storage Methods

To switch between MariaDB, CSV, or In-Memory RAM storage, modify the repository instantiation in `Main.java`:

```java
// Option 1: MariaDB SQL Storage
ExpenseRepository repository = new DatabaseExpenseRepository();

// Option 2: CSV File Storage
// ExpenseRepository repository = new CsvExpenseRepository();

// Option 3: In-Memory RAM Storage (ideal for unit testing)
// ExpenseRepository repository = new InMemoryExpenseRepository();

// Inject chosen repository into ExpenseManager
ExpenseManager manager = new ExpenseManager(repository);
```
