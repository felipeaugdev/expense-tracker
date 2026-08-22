package com.felipeaugdev;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExpenseManagerTest {

    private ExpenseManager manager;
    private InMemoryExpenseRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryExpenseRepository();
        manager = new ExpenseManager(repository);
    }

    @Test
    void testAddExpenseIncreasesListSize() {
        manager.addExpense(new BigDecimal("5.50"), "Coffee", "FOOD");

        List<Expense> expenses = manager.getAllExpenses();
        assertEquals(1, expenses.size());
        assertEquals("Coffee", expenses.get(0).getDescription());
        assertEquals(new BigDecimal("5.50"), expenses.get(0).getAmount());
    }

    @Test
    void testCalculateTotalExpenses() {
        manager.addExpense(new BigDecimal("10.00"), "Lunch", "FOOD");
        manager.addExpense(new BigDecimal("20.00"), "Bus Pass", "TRANSPORTATION");

        BigDecimal total = manager.getTotalExpenses(0);

        assertEquals(new BigDecimal("30.00"), total);
    }

    @Test
    void totalDeleteExpenseRemovesItem() {
        manager.addExpense(new BigDecimal("50.00"), "Groceries", "FOOD");

        boolean deleted = manager.deleteExpense(1);

        assertTrue(deleted);
        assertEquals(0, manager.getAllExpenses().size());
    }

    @Test
    void testDeleteNonExistentExpenseReturnsFalse() {
        boolean deleted = manager.deleteExpense(999);

        assertFalse(deleted);
    }

    @Test
    void testMonthOverMonthReportWithZeroPreviousMonth() {
        MonthOverMonthReport report = manager.getMonthOverMonthReport();

        assertEquals(new BigDecimal("0"), report.getPreviousTotal());
        assertEquals(new BigDecimal("0"), report.getPercentageChange());
    }

    @Test
    void testCategoryAggregationForEmptyExpenses() {
        var categoryTotals = manager.getTotalExpensesByCategory(0);

        assertTrue(categoryTotals.isEmpty());
    }

    @Test
    void testDateRangeFilteringAllTime() {
        manager.addExpense(new BigDecimal("25.00"), "Movie Ticket", "ENTERTAINMENT");
        manager.addExpense(new BigDecimal("12.00"), "Lunch", "FOOD");

        List<Expense> result = manager.getExpensesByDateRange(0);

        assertEquals(2, result.size());
    }

}
