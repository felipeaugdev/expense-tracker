package com.felipeaugdev;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseExpenseRepository implements ExpenseRepository {

    // Database connection parameters
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL",
            "jdbc:mariadb://localhost:3306/expense_tracker");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "expense_user");
    private static final String PASS = System.getenv().getOrDefault("DB_PASS", "change_me");

    // Helper method to open a database connection
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    @Override
    public List<Expense> loadExpenses() {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT id, amount, expense_date, description, category FROM expenses";

        // Try with resources to automatically close Connection, Statement and ResultSet
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            // Loop through each row in the query result
            while (rs.next()) {
                int id = rs.getInt("id");
                BigDecimal amount = rs.getBigDecimal("amount");
                LocalDate date = rs.getDate("expense_date").toLocalDate();
                String description = rs.getString("description");
                String category = rs.getString("category");

                expenses.add(new Expense(id, amount, date, description, category));
            }

        } catch (SQLException e) {
            System.out.println("Database error loading expenses: " + e.getMessage());
        }

        return expenses;

    }

    @Override
    public void save(Expense expense) {
        // Clear the table and re-sync with current list state
        String sql = "INSERT INTO expenses (id, amount, expense_date, description, category) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "amount = VALUES(amount), " +
                "expense_date = VALUES(expense_date), " +
                "description = VALUES(description), " +
                "category = VALUES(category)";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, expense.getId());
            stmt.setBigDecimal(2, expense.getAmount());
            stmt.setDate(3, java.sql.Date.valueOf(expense.getdDate()));
            stmt.setString(4, expense.getDescription());
            stmt.setString(5, expense.getCategory());

            stmt.executeUpdate();
            System.out.println("[DATABASE LOG] Saved expense ID " + expense.getId() + " to MariaDB.");

        } catch (SQLException e) {
            System.out.println("Database error saving expense: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("[DATABASE LOG] Deleted expense ID " + id + " from MariaDB.");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Database error deleting expense: " + e.getMessage());
        }

        return false;
    }

}
