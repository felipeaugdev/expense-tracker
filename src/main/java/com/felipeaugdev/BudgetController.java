package com.felipeaugdev;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.felipeaugdev.dto.BudgetRequest;
import com.felipeaugdev.dto.BudgetResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        Object principal = auth.getPrincipal();
        return principal instanceof User ? (User) principal : null;
    }

    @GetMapping
    public List<BudgetResponse> getBudgets() {
        return budgetService.getBudgetsForUser(getCurrentUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse createOrUpdateBudget(@Valid @RequestBody BudgetRequest request) {
        return budgetService.createOrUpdateBudget(getCurrentUser(), request.getCategory(), request.getAmount());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(getCurrentUser(), id);
    }
}
