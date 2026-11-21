package com.felipeaugdev;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.felipeaugdev.dto.ExpenseUpdateRequest;
import com.felipeaugdev.dto.ImportResult;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;
    private final CategoryRepository categoryRepository;
    private final RecurringExpenseService recurringExpenseService;

    public ExpenseService(ExpenseRepository repository,
            CategoryRepository categoryRepository,
            RecurringExpenseService recurringExpenseService) {

        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.recurringExpenseService = recurringExpenseService;
    }

    private static final LocalDate EARLIEST = LocalDate.of(1900, 1, 1);
    private static final LocalDate LATEST = LocalDate.of(9999, 12, 31);

    private LocalDate startDate(int days) {
        return days > 0 ? LocalDate.now().minusDays(days) : EARLIEST;
    }

    private BigDecimal sumBetween(User user, LocalDate from, LocalDate to) {
        BigDecimal sum = (user == null)
                ? repository.sumForGuests(from, to)
                : repository.sumForUser(user.getId(), from, to);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public Expense addExpense(BigDecimal amount, String description, String categoryName,
            User user, boolean recurring, Integer frequencyDays, LocalDate date) {

        Category category = categoryRepository.findVisibleByName(categoryName, user)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

        LocalDate expenseDate = (date != null) ? date : LocalDate.now();

        Expense newExpense = new Expense(amount, expenseDate, description, category, user);
        Expense saved = repository.save(newExpense);

        if (recurring && frequencyDays != null && frequencyDays > 0) {
            RecurringExpense template = new RecurringExpense(
                    amount,
                    description,
                    category,
                    frequencyDays,
                    LocalDate.now().plusDays(frequencyDays),
                    user);
            template = recurringExpenseService.create(template);

            saved.setRecurringExpense(template);
            saved = repository.save(saved);
        }

        return saved;
    }

    public Page<Expense> getExpenses(int days, User user, Pageable pageable) {
        if (days > 0) {
            LocalDate from = LocalDate.now().minusDays(days);
            return user == null
                    ? repository.findByUserIsNullAndDateGreaterThanEqual(from, pageable)
                    : repository.findByUserIdAndDateGreaterThanEqual(user.getId(), from, pageable);
        }
        return user == null
                ? repository.findByUserIsNull(pageable)
                : repository.findByUserId(user.getId(), pageable);
    }

    public Expense updateExpense(int id, ExpenseUpdateRequest request, User currentUser) {
        Expense expense = repository.findById(id)
                .filter(e -> belongsToUser(e, currentUser))
                .orElseThrow(() -> new RuntimeException("Expense not found or access denied"));

        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getDate() != null) {
            expense.setDate(request.getDate());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findVisibleByName(request.getCategory(), currentUser)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategory()));

            expense.setCategory(category);
        }

        if (request.getRecurring() != null) {
            if (request.getRecurring()) {
                int freq = request.getFrequencyDays() != null ? request.getFrequencyDays() : 30;

                RecurringExpense template = expense.getRecurringExpense();
                if (template == null) {
                    template = new RecurringExpense(
                            expense.getAmount(),
                            expense.getDescription(),
                            expense.getCategory(),
                            freq,
                            LocalDate.now().plusDays(freq),
                            currentUser);
                    template = recurringExpenseService.create(template);
                    expense.setRecurringExpense(template);
                } else {
                    template.setFrequencyDays(freq);
                    template.setActive(true);
                    template.setAmount(expense.getAmount());
                    template.setDescription(expense.getDescription());
                    template.setCategory(expense.getCategory());

                    recurringExpenseService.update(template);
                }
            } else {
                if (expense.getRecurringExpense() != null) {
                    recurringExpenseService.deactivate(
                            expense.getRecurringExpense().getId(), currentUser);
                    expense.setRecurringExpense(null);
                }
            }
        }

        return repository.save(Objects.requireNonNull(expense));
    }

    public boolean deleteExpense(int id, User user, boolean stopRecurring) {
        return repository.findById(id)
                .filter(expense -> belongsToUser(expense, user))
                .map(expense -> {
                    if (stopRecurring && expense.getRecurringExpense() != null) {
                        recurringExpenseService.deactivate(expense.getRecurringExpense().getId(), user);
                    }
                    repository.delete(Objects.requireNonNull(expense));
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
    public Map<String, BigDecimal> getTotalExpensesByCategory(int days, User user) {
        LocalDate from = startDate(days);
        List<CategoryTotal> rows = (user == null)
                ? repository.sumByCategoryForGuests(from, LATEST)
                : repository.sumByCategoryForUser(user.getId(), from, LATEST);

        Map<String, BigDecimal> categoryTotals = new TreeMap<>();
        for (CategoryTotal row : rows) {
            categoryTotals.put(row.getName(), row.getTotal());
        }
        return categoryTotals;
    }

    /**
     * Calculates the grand total of all expenses for a given time range.
     * 
     * @param days Number of days back to include (0 for All Time).
     * @return BigDecimal total of all expenses within the time range.
     */
    public BigDecimal getTotalExpenses(int days, User user) {
        return sumBetween(user, startDate(days), LATEST);
    }

    /**
     * Calculates total expenses for a specific calendar month.
     */
    public BigDecimal getTotalExpensesForMonth(YearMonth yearMonth, User user) {
        return sumBetween(user, yearMonth.atDay(1), yearMonth.atEndOfMonth());
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

    public List<Expense> getExpensesForExport(User user) {
        return user == null
                ? repository.findByUserIsNullOrderByIdAsc()
                : repository.findByUserIdOrderByIdAsc(user.getId());
    }

    public ImportResult importFromCsv(String csvContent, User user) {
        int added = 0;
        int failed = 0;

        String[] lines = csvContent.split("\\r?\\n");

        // Skip header if present
        int startIndex = 0;
        if (lines.length > 0 && lines[0].toLowerCase().contains("date")
                && lines[0].toLowerCase().contains("amount")) {
            startIndex = 1;
        }

        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty())
                continue;

            try {
                String[] parts = parseCsvLine(line);

                if (parts.length < 4) {
                    failed++;
                    continue;
                }

                LocalDate date = LocalDate.parse(parts[0].trim());
                String description = parts[1].trim();
                String categoryName = parts[2].trim().toUpperCase();
                BigDecimal amount = new BigDecimal(parts[3].trim());

                if (amount.compareTo(BigDecimal.ZERO) <= 0 || description.isEmpty()) {
                    failed++;
                    continue;
                }

                boolean categoryExists = categoryRepository
                        .findVisibleByName(categoryName, user)
                        .isPresent();

                String finalCategory = categoryExists ? categoryName : "OTHER";

                addExpense(amount, description, finalCategory, user, false, null, date);
                added++;

            } catch (Exception e) {
                failed++;
            }
        }

        return new ImportResult(added, failed);
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private boolean belongsToUser(Expense expense, User user) {
        if (user == null) {
            return expense.getUser() == null;
        }
        return expense.getUser() != null && expense.getUser().getId().equals(user.getId());
    }
}
