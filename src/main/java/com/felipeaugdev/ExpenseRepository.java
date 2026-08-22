package com.felipeaugdev;

import java.util.List;

public interface ExpenseRepository {

    /**
     * Loads all persisted expenses from storage.
     * 
     * @return List of Expense objects
     */
    List<Expense> loadExpenses();

    /**
     * Persists or updates a single expense in storage.
     * 
     * @param expenses The individual expense object to save.
     */
    void save(Expense expenses);

    /**
     * Deletes a single expense from storage by its unique ID
     * 
     * @param id The unique ID of the expense to remove
     * @return true if the expense was found and deleted, false otherwise
     */
    boolean deleteById(int id);

}
