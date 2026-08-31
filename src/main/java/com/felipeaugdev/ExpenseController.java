package com.felipeaugdev;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public List<Expense> getExpenses(@RequestParam(defaultValue = "0") int days) {
        return expenseService.getExpensesByDateRange(days);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expense addExpense(@RequestBody Expense expense) {
        return expenseService.addExpense(expense.getAmount(), expense.getDescription(), expense.getCategory());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable int id) {
        boolean deleted = expenseService.deleteExpense(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/category-totals")
    public Map<String, BigDecimal> getCategoryTotals(@RequestParam(defaultValue = "0") int days) {
        return expenseService.getTotalExpensesByCategory(days);
    }

    @GetMapping("/monthly-report")
    public MonthOverMonthReport getMonthlyReport() {
        return expenseService.getMonthOverMonthReport();
    }

}
