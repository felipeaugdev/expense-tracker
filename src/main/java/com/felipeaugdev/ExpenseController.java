package com.felipeaugdev;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.felipeaugdev.dto.ExpenseResponse;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Helper that returns the currently authenticated User,
     * or null when the request is from a guest.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }

    @GetMapping
    public List<ExpenseResponse> getExpenses(@RequestParam(defaultValue = "0") int days) {
        return expenseService.getExpensesByDateRange(days, getCurrentUser())
                .stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse addExpense(@RequestBody Expense expense) {
        Expense saved = expenseService.addExpense(
                expense.getAmount(), expense.getDescription(),
                expense.getCategory(), getCurrentUser());
        return ExpenseResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable int id) {
        boolean deleted = expenseService.deleteExpense(id, getCurrentUser());
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/category-totals")
    public Map<String, BigDecimal> getCategoryTotals(@RequestParam(defaultValue = "0") int days) {
        return expenseService.getTotalExpensesByCategory(days, getCurrentUser());
    }

    @GetMapping("/monthly-report")
    public MonthOverMonthReport getMonthlyReport() {
        return expenseService.getMonthOverMonthReport(getCurrentUser());
    }

}
