package com.felipeaugdev;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExpenseManager {

    private final ExpenseRepository repository;
    private List<Expense> expenses;
    private int nextId;

    // Dependency Injection: The repository is passed into the constructor from outside
    public ExpenseManager(ExpenseRepository repository) {
        // Load repository and existing expenses on app startup
        this.repository = repository;
        this.expenses = repository.loadExpenses();

        // Determine the correct nextId
        this.nextId = calculateNextId();
    }

    public void addExpense(BigDecimal amount, String description, String category) {
        Expense newExpense = new Expense(nextId, amount, LocalDate.now(), description, category);
        expenses.add(newExpense);
        nextId++;

        // Auto-save after adding
        repository.save(newExpense);
    }

    public List<Expense> getAllExpenses() {
        return expenses;
    }

    public boolean deleteExpense(int id) {
        boolean removedFromMemory = expenses.removeIf(expense -> expense.getId() == id);

        if (removedFromMemory) {
            repository.deleteById(id);
        }

        return removedFromMemory;
    }

    // HELPER: Calculates highest existing ID + 1 to avoid duplicate IDs
    private int calculateNextId() {
        int maxId = 0;
        for (Expense e : expenses) {
            if (e.getId() > maxId) {
                maxId = e.getId();
            }
        }
        return maxId + 1;
    }

    /**
     * Calculates total expenses grouped by category for a given date range.
     * 
     * @param days Number of days back to include (0 for All Time).
     * @return Map where the key is the category name and the value is the total amount.
     */
    public Map<String, BigDecimal> getTotalExpensesByCategory(int days) {
        List<Expense> filtered = getExpensesByDateRange(days);
        Map<String, BigDecimal> categoryTotals = new TreeMap<>();

        for (Expense expense : filtered) {
            String category = expense.getCategory();
            BigDecimal amount = expense.getAmount();

            BigDecimal currentTotal = categoryTotals.getOrDefault(category, BigDecimal.ZERO);
            categoryTotals.put(category, currentTotal.add(amount));
        }

        return categoryTotals;
    }

    /**
     * Calculates the grand total of all expenses for a given time range.
     * 
     * @param days Number of days back to include (0 for All Time).
     * @return BigDecimal total of all expenses within the time range.
     */
    public BigDecimal getTotalExpenses(int days) {
        List<Expense> filtered = getExpensesByDateRange(days);
        BigDecimal total = BigDecimal.ZERO;

        for (Expense expense : filtered) {
            total = total.add(expense.getAmount());
        }
        return total;
    }

    /**
     * Retrieve expenses filtered by date range.
     * 
     * @param days Number of days back to filter (7, 14, 30). Pass 0 for All Time.
     * @return List of matching Expense objects.
     */
    public List<Expense> getExpensesByDateRange(int days) {
        if (days <= 0) {
            return getAllExpenses();
        }

        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        List<Expense> filteredExpenses = new ArrayList<>();

        for (Expense expense : expenses) {
            if (!expense.getDate().isBefore(cutoffDate)) {
                filteredExpenses.add(expense);
            }
        }

        return filteredExpenses;
    }

    /**
     * Calculates total expenses for a specific calendar month.
     */
    public BigDecimal getTotalExpensesForMonth(YearMonth yearMonth) {
        BigDecimal total = BigDecimal.ZERO;

        for (Expense expense : expenses) {
            YearMonth expenseMonth = YearMonth.from(expense.getDate());
            if (expenseMonth.equals(yearMonth)) {
                total = total.add(expense.getAmount());
            }
        }

        return total;
    }

    /**
     * Generates a Month-over-Month report comparing the current month against the previous month.
     */
    public MonthOverMonthReport getMonthOverMonthReport() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        BigDecimal currentTotal = getTotalExpensesForMonth(currentMonth);
        BigDecimal previousTotal = getTotalExpensesForMonth(previousMonth);

        BigDecimal difference = currentTotal.subtract(previousTotal);

        BigDecimal percentageChange = BigDecimal.ZERO;
        if (previousTotal.compareTo(BigDecimal.ZERO) > 0) {
            percentageChange = difference
                    .divide(previousTotal, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new MonthOverMonthReport(previousMonth, currentMonth,
                previousTotal, currentTotal,
                difference, percentageChange);
    }

}
