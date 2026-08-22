package com.felipeaugdev;

import java.util.ArrayList;
import java.util.List;

public class InMemoryExpenseRepository implements ExpenseRepository {

    private List<Expense> memoryStorage;

    // Default constructor
    public InMemoryExpenseRepository() {
        this.memoryStorage = new ArrayList<>();
    }

    // Optional constructor for seeding initial dummy data (for testing)
    public InMemoryExpenseRepository(List<Expense> initialData) {
        this.memoryStorage = new ArrayList<>(initialData);
    }

    @Override
    public List<Expense> loadExpenses() {
        // Return a copy so internal storage isn't directly modified outside this class
        return new ArrayList<>(memoryStorage);
    }

    @Override
    public void save(Expense expense) {
        // Update in-memory state
        boolean updated = false;
        for (int i = 0; i < memoryStorage.size(); i++) {
            if (memoryStorage.get(i).getId() == expense.getId()) {
                memoryStorage.set(i, expense);
                updated = true;
                break;
            }
        }

        if (!updated) {
            memoryStorage.add(expense);
        }

        System.out.println("[RAM LOG] Expense ID " + expense.getId() + " saved to RAM.");
    }

    @Override
    public boolean deleteById(int id) {
        boolean removed = memoryStorage.removeIf(e -> e.getId() == id);

        if (removed) {
            System.out.println("[RAM LOG] Expense ID " + id + " removed from RAM.");
        }

        return removed;
    }

}
