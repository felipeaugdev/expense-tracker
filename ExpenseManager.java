import java.math.BigDecimal;
import java.time.LocalDate;
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

    public BigDecimal getTotalExpenses() {
        BigDecimal total = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            total = total.add(expense.getAmount());
        }
        return total;
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
     * Group total expenses by category
     * 
     * @return Map where the key is the category name and the value is the total amount.
     */
    public Map<String, BigDecimal> getTotalExpensesByCategory() {
        Map<String, BigDecimal> categoryTotals = new TreeMap<>();

        for (Expense expense : expenses) {
            String category = expense.getCategory();
            BigDecimal amount = expense.getAmount();

            BigDecimal currentTotal = categoryTotals.getOrDefault(category, BigDecimal.ZERO);
            categoryTotals.put(category, currentTotal.add(amount));
        }

        return categoryTotals;
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
            if (!expense.getdDate().isBefore(cutoffDate)) {
                filteredExpenses.add(expense);
            }
        }

        return filteredExpenses;
    }

}
