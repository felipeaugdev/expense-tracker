package com.felipeaugdev.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseUpdateRequest {

    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private String category;

    // --- GETTERS AND SETTERS ---
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
