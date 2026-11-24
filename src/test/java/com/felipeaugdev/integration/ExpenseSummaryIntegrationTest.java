package com.felipeaugdev.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;

class ExpenseSummaryIntegrationTest extends AbstractIntegrationTest {

    private ResultActions getCategoryTotals(String token, String query) throws Exception {
        var request = get("/api/expenses/category-totals?" + query);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    @Test
    void categoryTotalsAddUpPerCategory() throws Exception {
        String alice = registerAndLogin("alice");
        createExpense(alice, "Lunch", "FOOD", 10.00);
        createExpense(alice, "Dinner", "FOOD", 15.00);
        createExpense(alice, "Taxi", "TRANSPORTATION", 20.00);

        getCategoryTotals(alice, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.FOOD").value(25.0))
                .andExpect(jsonPath("$.TRANSPORTATION").value(20.0));
    }

    @Test
    void categoryTotalsOnlyCountTheirOwner() throws Exception {
        String alice = registerAndLogin("alice");
        String bob = registerAndLogin("bob");
        createExpense(alice, "Lunch", "FOOD", 10.00);
        createExpense(bob, "Groceries", "FOOD", 100.00);
        createExpense(bob, "Power", "UTILITIES", 60.00);

        getCategoryTotals(alice, "")
                .andExpect(jsonPath("$.FOOD").value(10.0))
                .andExpect(jsonPath("$.UTILITIES").doesNotExist());
        getCategoryTotals(bob, "")
                .andExpect(jsonPath("$.FOOD").value(100.0))
                .andExpect(jsonPath("$.UTILITIES").value(60.0));
    }

    @Test
    void categoryTotalsRespectTheDaysFilter() throws Exception {
        String alice = registerAndLogin("alice");
        createExpense(alice, "Recent", "FOOD", 10.00);
        JsonNode oldExpense = createExpense(alice, "Old", "FOOD", 20.00);
        moveExpenseToDate(alice, oldExpense, LocalDate.now().minusDays(60));

        getCategoryTotals(alice, "days=30").andExpect(jsonPath("$.FOOD").value(10.0));
        getCategoryTotals(alice, "days=0").andExpect(jsonPath("$.FOOD").value(30.0));
    }

    @Test
    void monthlyReportComparesWithThePreviousMonth() throws Exception {
        String alice = registerAndLogin("alice");
        JsonNode lastMonth = createExpense(alice, "Last month", "FOOD", 100.00);
        moveExpenseToDate(alice, lastMonth, LocalDate.now().minusMonths(1));
        createExpense(alice, "This month", "FOOD", 150.00);

        mockMvc.perform(get("/api/expenses/monthly-report")
                .header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previousTotal").value(100.0))
                .andExpect(jsonPath("$.currentTotal").value(150.0))
                .andExpect(jsonPath("$.difference").value(50.0))
                .andExpect(jsonPath("$.percentageChange").value(50.0));
    }

    @Test
    @SuppressWarnings("null")
    void exportContainsOnlyTheUsersOwnExpenses() throws Exception {
        String alice = registerAndLogin("alice");
        String bob = registerAndLogin("bob");
        createExpense(alice, "Lunch, with friends", "FOOD", 12.50);
        createExpense(bob, "Bob groceries", "FOOD", 40.00);

        mockMvc.perform(get("/api/expenses/export")
                .header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("Date,Description,Category,Amount")))
                .andExpect(content().string(containsString("\"Lunch, with friends\",FOOD")))
                .andExpect(content().string(not(containsString("Bob groceries"))));
    }
}
