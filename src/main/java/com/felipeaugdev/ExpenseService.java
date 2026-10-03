package com.felipeaugdev;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public Expense addExpense(BigDecimal amount, String description, String category, User user) {
        Expense newExpense = new Expense(amount, LocalDate.now(), description, category, user);
        return repository.save(newExpense);
    }

    public List<Expense> getAllExpenses(User user) {
        List<Expense> all = repository.findAll();
        return filterByUser(all, user);
    }

    @SuppressWarnings("null")
    public boolean deleteExpense(int id, User user) {
        return repository.findById(id)
                .filter(expense -> belongsToUser(expense, user))
                .map(expense -> {
                    repository.delete(expense);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Calculates total expenses grouped by category for a given date range.
     * 
     * @param days Number of days back to include (0 for All Time).
     * @return Map where the key is the category name and the value is the total amount.
     */
    @SuppressWarnings("null")
    public Map<String, BigDecimal> getTotalExpensesByCategory(int days, User user) {
        List<Expense> filtered = getExpensesByDateRange(days, user);
        Map<String, BigDecimal> categoryTotals = new TreeMap<>();

        for (Expense expense : filtered) {
            String category = expense.getCategory();
            BigDecimal amount = expense.getAmount();
            categoryTotals.merge(category, amount, BigDecimal::add);
        }
        return categoryTotals;
    }

    /**
     * Calculates the grand total of all expenses for a given time range.
     * 
     * @param days Number of days back to include (0 for All Time).
     * @return BigDecimal total of all expenses within the time range.
     */
    @SuppressWarnings("null")
    public BigDecimal getTotalExpenses(int days, User user) {
        return getExpensesByDateRange(days, user).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retrieve expenses filtered by date range.
     * 
     * @param days Number of days back to filter (7, 14, 30). Pass 0 for All Time.
     * @return List of matching Expense objects.
     */
    public List<Expense> getExpensesByDateRange(int days, User user) {
        List<Expense> all = getAllExpenses(user);

        if (days <= 0) {
            return all;
        }

        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        return all.stream()
                .filter(e -> !e.getDate().isBefore(cutoffDate))
                .collect(Collectors.toList());
    }

    /**
     * Calculates total expenses for a specific calendar month.
     */
    @SuppressWarnings("null")
    public BigDecimal getTotalExpensesForMonth(YearMonth yearMonth, User user) {
        return getAllExpenses(user).stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(yearMonth))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Generates a Month-over-Month report comparing the current month against the previous month.
     */
    public MonthOverMonthReport getMonthOverMonthReport(User user) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        BigDecimal currentTotal = getTotalExpensesForMonth(currentMonth, user);
        BigDecimal previousTotal = getTotalExpensesForMonth(previousMonth, user);

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

    private List<Expense> filterByUser(List<Expense> expenses, User user) {
        if (user == null) {
            return expenses.stream()
                    .filter(e -> e.getUser() == null)
                    .collect(Collectors.toList());
        }
        return expenses.stream()
                .filter(e -> e.getUser() != null && e.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
    }

    private boolean belongsToUser(Expense expense, User user) {
        if (user == null) {
            return expense.getUser() == null;
        }
        return expense.getUser() != null && expense.getUser().getId().equals(user.getId());
    }
}
