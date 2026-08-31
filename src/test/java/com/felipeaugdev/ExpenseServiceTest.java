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
    void testAddExpenseSavesAndReturnsExpense() {
        Expense sample = new Expense(1, new BigDecimal("5.50"), LocalDate.now(), "Coffee", "FOOD");
        when(repository.save(any(Expense.class))).thenReturn(sample);

        Expense created = service.addExpense(new BigDecimal("5.50"), "Coffee", "FOOD");

        assertNotNull(created);
        assertEquals("Coffee", created.getDescription());
        assertEquals(new BigDecimal("5.50"), created.getAmount());
        verify(repository, times(1)).save(any(Expense.class));
    }

    @Test
    void testCalculateTotalExpenses() {
        Expense e1 = new Expense(1, new BigDecimal("10.00"), LocalDate.now(), "Lunch", "FOOD");
        Expense e2 = new Expense(1, new BigDecimal("20.00"), LocalDate.now(), "Bus Pass", "TRANSPORTATION");
        when(repository.findAll()).thenReturn(List.of(e1, e2));

        BigDecimal total = service.getTotalExpenses(0);

        assertEquals(new BigDecimal("30.00"), total);
    }

    @Test
    void testDeleteExistingExpenseReturnsTrue() {
        when(repository.existsById(1)).thenReturn(true);

        boolean deleted = service.deleteExpense(1);

        assertTrue(deleted);
        verify(repository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteNonExistentExpenseReturnsFalse() {
        when(repository.existsById(999)).thenReturn(false);

        boolean deleted = service.deleteExpense(999);

        assertFalse(deleted);
        verify(repository, never()).deleteById(anyInt());
    }

    @Test
    void testMonthOverMonthReportWithZeroPreviousMonth() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        MonthOverMonthReport report = service.getMonthOverMonthReport();

        assertEquals(new BigDecimal("0"), report.getPreviousTotal());
        assertEquals(new BigDecimal("0"), report.getPercentageChange());
    }

    @Test
    void testCategoryAggregationForEmptyExpenses() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        var categoryTotals = service.getTotalExpensesByCategory(0);

        assertTrue(categoryTotals.isEmpty());
    }

    @Test
    void testDateRangeFilteringAllTime() {
        Expense e1 = new Expense(1, new BigDecimal("25.00"), LocalDate.now(), "Movie Ticket", "ENTERTAINMENT");
        Expense e2 = new Expense(2, new BigDecimal("12.00"), LocalDate.now(), "Lunch", "FOOD");
        when(repository.findAll()).thenReturn(List.of(e1, e2));

        List<Expense> result = service.getExpensesByDateRange(0);

        assertEquals(2, result.size());
    }
}
