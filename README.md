# Expense Tracker REST API & Dashboard

A full-stack personal expense tracking application with real-time analytics. Built with Java 21, Spring Boot, MariaDB, Docker, and a modern Vanilla JS/Tailwind CSS dashboard.

## Features

- **Web Dashboard:** Interactive single-page dashboard displaying real-time monthly metrics, spending category charts, and a transaction table.
- **Expense Management:** Full CRUD operations for creating, viewing, and deleting expense records.
- **Date Range Filtering:** Filter transactions by rolling time windows (e.g., last 7, 14, or 30 days).
- **Category Breakdown:** Aggregated spending totals grouped by expense category.
- **Month-over-Month Reports:** Comparative analysis between current and previous calendar month spending.
- **Dockerized Environment:** Multi-stage Docker build packaging both Spring Boot backend and MariaDB database via Docker Compose.
- **API Documentation:** Interactive Swagger UI generated via SpringDoc OpenAPI.

## Tech Stack

- **Language & Framework:** Java 21 LTS, Spring Boot 3.3 (Spring Web, Spring Data JPA)
- **Frontend:** HTML5, Tailwind CSS (CDN), Vanilla JavaScript (ES6+), Chart.js
- **Database:** MariaDB 11
- **Build & Package:** Maven, Docker, Docker Compose
- **Testing:** JUnit 5, Mockito, MockMvc
- **Documentation:** SpringDoc OpenAPI

## API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/expenses` | Fetch all expenses (supports optional `?days=N` query param) |
| `POST` | `/api/expenses` | Create a new expense |
| `DELETE` | `/api/expenses/{id}` | Delete an expense by ID |
| `GET` | `/api/expenses/category-totals` | Get spending aggregated by category |
| `GET` | `/api/expenses/monthly-report` | Get month-over-month report metrics |

## Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows/macOS) or [Docker Engine + Docker Compose](https://docs.docker.com/engine/install/) (Linux).

### Running the Application

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/felipeaugdev/expense-tracker.git](https://github.com/felipeaugdev/expense-tracker.git)
   cd expense-tracker
   ```

2. **Start the stack:**
   ```bash
   docker compose up -d
   ```

3. **Access the application:**
   - **Web Dashboard:** `http://localhost:8080/`
   - **Swagger UI:** `http://localhost:8080/swagger-ui.html`
   - **REST API Base:** `http://localhost:8080/api/expenses`

4. **Stop the stack:**
   ```bash
   docker compose down
   ```
   *(Run `docker compose down -v` to also remove the persistent database volume.)*

## Running Tests

To run unit and controller tests locally (requires JDK 21+):

```bash
mvn test
```
