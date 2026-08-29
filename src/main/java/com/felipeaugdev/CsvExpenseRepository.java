package com.felipeaugdev;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CsvExpenseRepository implements ExpenseRepository {

    private final String fileName;

    // Default constructor uses "expenses.csv"
    public CsvExpenseRepository() {
        this("expenses.csv");
    }

    // Custom constructor lets you specify a custom file name
    public CsvExpenseRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public List<Expense> loadExpenses() {
        List<Expense> loadedExpenses = new ArrayList<>();
        File file = new File(fileName);

        if (!file.exists()) {
            return loadedExpenses;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    int id = Integer.parseInt(parts[0]);
                    BigDecimal amount = new BigDecimal(parts[1]);
                    LocalDate date = LocalDate.parse(parts[2]);
                    String description = parts[3];
                    String category = parts[4];

                    loadedExpenses.add(new Expense(id, amount, date, description, category));
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error loading expenses file: " + e.getMessage());
        }

        return loadedExpenses;

    }

    @Override
    public void save(Expense expense) {
        List<Expense> current = loadExpenses();
        boolean updated = false;

        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getId() == expense.getId()) {
                current.set(i, expense);
                updated = true;
                break;
            }
        }

        if (!updated) {
            current.add(expense);
        }

        writeAllToFile(current);
    }

    @Override
    public boolean deleteById(int id) {
        List<Expense> current = loadExpenses();
        boolean removed = current.removeIf(e -> e.getId() == id);

        if (removed) {
            writeAllToFile(current);
        }

        return removed;
    }

    private void writeAllToFile(List<Expense> expenses) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (Expense e : expenses) {
                writer.println(e.getId() + "," +
                        e.getAmount() + "," +
                        e.getDate() + "," +
                        escapeCsv(e.getDescription()) + "," +
                        escapeCsv(e.getCategory()));
            }
            // System.out.println("Data saved successfully to " + fileName);
        } catch (IOException e) {
            System.out.println("Error writing to CSV: " + e.getMessage());
        }
    }

    private String escapeCsv(String data) {
        return data.replace(",", " ");
    }

}
