# Java CLI Expense Tracker

A modular, architecture-focused Java command-line application for tracking and analyzing personal expenses. Built to demonstrate clean object-oriented design, loose coupling, the Data Access Object (DAO) pattern, build automation with Maven, and database persistence using JDBC and MariaDB.

---

## 🏛️ Key Architectural Highlights

* **Loose Coupling via Interfaces:** Business logic interacts solely with an `ExpenseRepository` abstraction. Storage back-ends can be swapped seamlessly without modifying core domain logic.
* **Dependency Injection (DI):** Hand-rolled constructor injection decoupling `ExpenseManager` from concrete storage instantiation.
* **Pluggable Storage Implementations:**
  * `DatabaseExpenseRepository`: SQL persistence via MariaDB and JDBC.
  * `CsvExpenseRepository`: File-based storage reading and writing with standard CSV format.
  * `InMemoryExpenseRepository`: Zero-disk-access RAM storage designed for high-speed unit testing.
* **Granular SQL Operations:** Utilizes MariaDB `UPSERT` (`ON DUPLICATE KEY UPDATE`) and targeted `DELETE` operations for $O(1)$ updates instead of full table overwrites.
* **Defensive Input & Domain Validation:** Strong typing using custom `Category` enums, immutable `BigDecimal` math for currency scaling, and safe date handling using Java's `java.time` API.
* **Environment Variable Credentials:** Security-sensitive parameters (like database credentials) are externalized via environment variables.

---

## 💡 Features & Analytics

* **Interactive Category Picker:** Choose from standardized categories or supply a custom string via the `OTHER` option.
* **Time-Series Date Filtering:** View raw expenses or aggregated totals filtered by custom windows (*All Time*, *Last 7 Days*, *Last 14 Days*, *Last 30 Days*).
* **Category Aggregation:** Automatically groups spending by category and outputs totals using sorted data structures (`TreeMap`).
* **Monthly Comparison Analytics:** Generate a visual dashboard card comparing spending metrics between the current and previous calendar months, complete with delta variances and percentage indicators.

---

## 🛠️ Tech Stack & Requirements

* **Language:** Java 21 (Core Java / JDBC)
* **Build System:** Apache Maven
* **Database:** MariaDB / MySQL
* **JDBC Driver:** MariaDB Java Client (`mariadb-java-client`)
* **Testing:** JUnit 5 & Mockito
* **Environment:** Linux, Windows, macOS

---

## 📁 Project Structure
```text
expense-tracker/
├── pom.xml                               # Maven Project Descriptor & Dependencies
├── schema.sql                            # MariaDB Database Bootstrap Script
├── .gitignore
├── README.md
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── felipeaugdev/
    │               ├── Category.java
    │               ├── CsvExpenseRepository.java
    │               ├── DatabaseExpenseRepository.java
    │               ├── Expense.java
    │               ├── ExpenseManager.java
    │               ├── ExpenseRepository.java
    │               ├── InMemoryExpenseRepository.java
    │               ├── Main.java
    │               └── MonthOverMonthReport.java
    └── test/
        └── java/
            └── com/
                └── felipeaugdev/         # Unit Tests (JUnit 5 & Mockito)
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

### 4. Build & Run the Application
Set your database password in your terminal environment and compile/run the app using Maven:

```bash
# Set your environment variable
export DB_PASS="your_password"

# Compile the project
mvn compile

# Run the application
mvn exec:java -Dexec.mainClass="com.felipeaugdev.Main"
```

---

## 🧪 Running Automated Tests

Execute the test suite with JUnit 5 and Mockito:

```bash
mvn test
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