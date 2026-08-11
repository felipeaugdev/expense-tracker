import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Optional seeding of existing CSV data to the in-memory repository for testing (pass csvData to InMemoryExpenseRepository()):
        // List<Expense> csvData = new CsvExpenseRepository().loadExpenses();

        ExpenseRepository repository = new DatabaseExpenseRepository();
        // ExpenseRepository repository = new CsvExpenseRepository();
        // ExpenseRepository repository = new InMemoryExpenseRepository();

        ExpenseManager manager = new ExpenseManager(repository);

        boolean running = true;

        System.out.println("=== PERSONAL EXPENSE TRACKER ===");

        while (running) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Add Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. Delete Expense");
            System.out.println("4. View Total Expenses");
            System.out.println("5. Exit");

            int choice = readInt(scanner, "> ");

            switch (choice) {
                case 1:
                    handleAddExpense(scanner, manager);
                    break;
                case 2:
                    handleViewExpenses(manager);
                    break;
                case 3:
                    handleDeleteExpense(scanner, manager);
                    break;
                case 4:
                    handleViewTotal(manager);
                    break;
                case 5:
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    // --- INPUT VALIDATION METHODS ---

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
    }

    private static BigDecimal readBigDecimal(Scanner scanner, String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine();

            try {
                BigDecimal amount = new BigDecimal(input);

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Amount must be a positive value. Try again.");
                    continue;
                }

                return amount.setScale(2, RoundingMode.HALF_UP);

            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Please enter a valid number (e.g. 12.50 or 120).");
            }
        }
    }

    // --- HELPER METHODS ---

    private static void handleAddExpense(Scanner scanner, ExpenseManager manager) {
        BigDecimal amount = readBigDecimal(scanner, "Enter amount (e.g. 12.50): ");

        System.out.println("Enter a description: ");
        String description = scanner.nextLine();

        System.out.println("Enter category: ");
        String category = scanner.nextLine();

        manager.addExpense(amount, description, category);
        System.out.println("\nExpense added successfully!");
    }

    private static void handleViewExpenses(ExpenseManager manager) {
        List<Expense> expenses = manager.getAllExpenses();
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded yet.");
            return;
        }

        System.out.println("\n--- ALL EXPENSES ---");
        for (Expense e : expenses) {
            System.out.println("ID: " + e.getId() +
                    " | Date: " + e.getdDate() +
                    " | Amount: $" + e.getAmount() +
                    " | Description: " + e.getDescription() +
                    " | Category: " + e.getCategory());
        }
    }

    private static void handleDeleteExpense(Scanner scanner, ExpenseManager manager) {
        int id = readInt(scanner, "Enter Expense ID to delete: ");

        boolean deleted = manager.deleteExpense(id);
        if (deleted) {
            System.out.println("\nExpense #" + id + " deleted successfully.");
        } else {
            System.out.println("\nError: Expense with ID " + id + " not found.");
        }
    }

    private static void handleViewTotal(ExpenseManager manager) {
        BigDecimal total = manager.getTotalExpenses();
        System.out.println("Total Expenses: " + total);
    }

}
