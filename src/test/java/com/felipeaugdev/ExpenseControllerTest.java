package com.felipeaugdev;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @Test
    void testGetExpensesReturnsJsonList() throws Exception {
        Expense expense = new Expense(1, new BigDecimal("15.00"), LocalDate.now(), "Coffee", "FOOD");
        when(expenseService.getExpensesByDateRange(0)).thenReturn(List.of(expense));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Coffee"))
                .andExpect(jsonPath("$[0].amount").value(15.00));
    }

    @Test
    @SuppressWarnings("null")
    void testAddExpenseReturnsCreatedStatus() throws Exception {
        Expense expense = new Expense(1, new BigDecimal("25.50"), LocalDate.now(), "Dinner", "FOOD");
        when(expenseService.addExpense(any(BigDecimal.class), eq("Dinner"), eq("FOOD")))
                .thenReturn(expense);

        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Dinner"));
    }

    @Test
    void testDeleteExpenseExistingReturnsNoContent() throws Exception {
        when(expenseService.deleteExpense(1)).thenReturn(true);

        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteExpenseNotFoundReturns404() throws Exception {
        when(expenseService.deleteExpense(999)).thenReturn(false);

        mockMvc.perform(delete("/api/expenses/999"))
                .andExpect(status().isNotFound());
    }
}
