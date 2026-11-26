package com.felipeaugdev.integration;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

interface SchemaConstraintTest {

    JdbcTemplate jdbc();

    private long insertUser() {
        String username = "schema_" + UUID.randomUUID();
        jdbc().update("INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
                username, "x", "USER");
        return jdbc().queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    @Test
    default void usernamesMustBeUnique() {
        String username = "dup_" + UUID.randomUUID();
        jdbc().update("INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
                username, "x", "USER");

        assertThrows(DataIntegrityViolationException.class, () -> jdbc().update(
                "INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
                username, "y", "USER"));
    }

    @Test
    default void categoryNamesAreUniquePerUserNotGlobally() {
        long alice = insertUser();
        long bob = insertUser();

        jdbc().update("INSERT INTO categories (name, user_id) VALUES (?, ?)", "GYM", alice);
        jdbc().update("INSERT INTO categories (name, user_id) VALUES (?, ?)", "GYM", bob);

        assertThrows(DataIntegrityViolationException.class, () -> jdbc().update(
                "INSERT INTO categories (name, user_id) VALUES (?, ?)", "GYM", alice));
    }

    @Test
    @SuppressWarnings("null")
    default void aUserCanOnlyHaveOneBudgetPerCategory() {
        long alice = insertUser();
        long foodId = jdbc().queryForObject(
                "SELECT id FROM categories WHERE name = 'FOOD' AND user_id IS NULL", Long.class);

        jdbc().update("INSERT INTO budgets (amount, category_id, user_id) VALUES (?, ?, ?)",
                100, foodId, alice);

        assertThrows(DataIntegrityViolationException.class, () -> jdbc().update(
                "INSERT INTO budgets (amount, category_id, user_id) VALUES (?, ?, ?)",
                200, foodId, alice));
    }

    @Test
    default void expensesMustReferenceAnExistingCategory() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbc().update(
                "INSERT INTO expenses (amount, expense_date, description, category_id) "
                        + "VALUES (?, CURRENT_DATE, ?, ?)",
                5, "Orphan", -1L));
    }
}
