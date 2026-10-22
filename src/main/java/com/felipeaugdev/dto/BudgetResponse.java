package com.felipeaugdev.dto;

import java.math.BigDecimal;

public class BudgetResponse {

    private Long id;
    private String category;
    private BigDecimal limit;
    private BigDecimal spent;
    private BigDecimal remaining;
    private double percentage;

    public BudgetResponse() {
    }

    public BudgetResponse(Long id, String category, BigDecimal limit,
            BigDecimal spent, BigDecimal remaining, double percentage) {

        this.id = id;
        this.category = category;
        this.limit = limit;
        this.spent = spent;
        this.remaining = remaining;
        this.percentage = percentage;
    }

    // GETTERS & SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public void setSpent(BigDecimal spent) {
        this.spent = spent;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public void setRemaining(BigDecimal remaining) {
        this.remaining = remaining;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
