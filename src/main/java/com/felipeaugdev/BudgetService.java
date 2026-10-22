package com.felipeaugdev;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.felipeaugdev.dto.BudgetResponse;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public BudgetService(BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository) {

        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<BudgetResponse> getBudgetsForUser(User user) {
        if (user == null) {
            return List.of();
        }

        List<Budget> budgets = budgetRepository.findByUserId(user.getId());
        YearMonth currentMonth = YearMonth.now();

        return budgets.stream()
                .map(budget -> {
                    BigDecimal spent = calculateSpent(user, budget.getCategory(), currentMonth);
                    BigDecimal limit = budget.getAmount();
                    BigDecimal remaining = limit.subtract(spent).max(BigDecimal.ZERO);

                    double percentage = 0;
                    if (limit.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = spent
                                .divide(limit, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue();
                    }

                    return new BudgetResponse(
                            budget.getId(),
                            budget.getCategory().getName(),
                            limit,
                            spent,
                            remaining,
                            percentage);
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateSpent(User user, Category category, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return expenseRepository.findAll().stream()
                .filter(e -> e.getUser() != null && e.getUser().getId().equals(user.getId()))
                .filter(e -> e.getCategory() != null && e.getCategory().getId().equals(category.getId()))
                .filter(e -> !e.getDate().isBefore(start) && !e.getDate().isAfter(end))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
