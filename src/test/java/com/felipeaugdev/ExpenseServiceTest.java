package com.felipeaugdev;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository repository;

    @InjectMocks
    private ExpenseService service;

    @Test
    @SuppressWarnings("null")
    void testAddExpenseSavesAndReturnsExpense() {
        Expense sample = new Expense(new BigDecimal("5.50"), LocalDate.now(), "Coffee", "FOOD");
        when(repository.save(any(Expense.class))).thenReturn(sample);

        Expense created = service.addExpense(new BigDecimal("5.50"), "Coffee", "FOOD", null);

        assertNotNull(created);
        assertEquals("Coffee", created.getDescription());
        assertEquals(new BigDecimal("5.50"), created.getAmount());
        verify(repository, times(1)).save(any(Expense.class));
    }

    @Test
    void testCalculateTotalExpenses() {
        Expense e1 = new Expense(new BigDecimal("10.00"), LocalDate.now(), "Lunch", "FOOD");
        Expense e2 = new Expense(new BigDecimal("20.00"), LocalDate.now(), "Bus Pass", "TRANSPORTATION");
        when(repository.findAll()).thenReturn(List.of(e1, e2));

        BigDecimal total = service.getTotalExpenses(0, null);

        assertEquals(new BigDecimal("30.00"), total);
    }

    @Test
    void testDeleteExistingExpenseReturnsTrue() {
        Expense expense = new Expense(new BigDecimal("10.00"), LocalDate.now(), "Test", "FOOD");
        when(repository.findById(1)).thenReturn(java.util.Optional.of(expense));

        boolean deleted = service.deleteExpense(1, null);

        assertTrue(deleted);
        verify(repository).delete(expense);
    }

    @Test
    @SuppressWarnings("null")
    void testDeleteNonExistentExpenseReturnsFalse() {
        when(repository.findById(999)).thenReturn(java.util.Optional.empty());

        boolean deleted = service.deleteExpense(999, null);

        assertFalse(deleted);
        verify(repository, never()).delete(any());
    }

    @Test
    void testMonthOverMonthReportWithZeroPreviousMonth() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        MonthOverMonthReport report = service.getMonthOverMonthReport(null);

        assertEquals(new BigDecimal("0"), report.getPreviousTotal());
        assertEquals(new BigDecimal("0"), report.getPercentageChange());
    }

    @Test
    void testCategoryAggregationForEmptyExpenses() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        var categoryTotals = service.getTotalExpensesByCategory(0, null);

        assertTrue(categoryTotals.isEmpty());
    }

    @Test
    void testDateRangeFilteringAllTime() {
        Expense e1 = new Expense(new BigDecimal("25.00"), LocalDate.now(), "Movie Ticket", "ENTERTAINMENT");
        Expense e2 = new Expense(new BigDecimal("12.00"), LocalDate.now(), "Lunch", "FOOD");
        when(repository.findAll()).thenReturn(List.of(e1, e2));

        List<Expense> result = service.getExpensesByDateRange(0, null);

        assertEquals(2, result.size());
    }
}
