import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExpenseManager {

    private final ExpenseRepository repository;
    private List<Expense> expenses;
    private int nextId;

    // Dependency Injection: The repository is passed into the constructor from outside
    public ExpenseManager(ExpenseRepository repository) {
        // 1. Load repository and existing expenses on app startup
        this.repository = repository;
        this.expenses = repository.loadExpenses();

        // 2. Determine the correct nextId based on existing data
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

}
