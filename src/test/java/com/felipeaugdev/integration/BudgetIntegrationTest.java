package com.felipeaugdev.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class BudgetIntegrationTest extends AbstractIntegrationTest {

    @SuppressWarnings("null")
    private ResultActions setBudget(String token, String category, double amount) throws Exception {
        var request = post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("category", category, "amount", amount)));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    private ResultActions getBudgets(String token) throws Exception {
        var request = get("/api/budgets");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    @Test
    void budgetShowsSpendingForTheCurrentMonth() throws Exception {
        String alice = registerAndLogin("alice");
        setBudget(alice, "FOOD", 100.00).andExpect(status().isCreated());
        createExpense(alice, "Groceries", "FOOD", 40.00);

        getBudgets(alice)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("FOOD"))
                .andExpect(jsonPath("$[0].limit").value(100.0))
                .andExpect(jsonPath("$[0].spent").value(40.0))
                .andExpect(jsonPath("$[0].remaining").value(60.0))
                .andExpect(jsonPath("$[0].percentage").value(40.0));
    }

    @Test
    void settingTheSameBudgetTwiceUpdatesInsteadOfDuplicating() throws Exception {
        String alice = registerAndLogin("alice");

        setBudget(alice, "FOOD", 100.00).andExpect(status().isCreated());
        setBudget(alice, "FOOD", 250.00).andExpect(status().isCreated());

        getBudgets(alice)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].limit").value(250.0));
    }

    @Test
    void budgetsArePrivateToTheirOwner() throws Exception {
        String alice = registerAndLogin("alice");
        String bob = registerAndLogin("bob");
        setBudget(alice, "FOOD", 100.00).andExpect(status().isCreated());

        getBudgets(alice).andExpect(jsonPath("$.length()").value(1));
        getBudgets(bob).andExpect(jsonPath("$.length()").value(0));
        getBudgets(null).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void sameCustomCategoryNameGivesEachUserTheirOwnBudget() throws Exception {
        String alice = registerAndLogin("alice");
        String bob = registerAndLogin("bob");
        createCategory(alice, "Gym").andExpect(status().isCreated());
        createCategory(bob, "Gym").andExpect(status().isCreated());

        setBudget(alice, "GYM", 50.00).andExpect(status().isCreated());
        setBudget(bob, "GYM", 80.00).andExpect(status().isCreated());
        createExpense(alice, "Alice gym fee", "GYM", 20.00);

        getBudgets(alice)
                .andExpect(jsonPath("$[0].limit").value(50.0))
                .andExpect(jsonPath("$[0].spent").value(20.0));
        getBudgets(bob)
                .andExpect(jsonPath("$[0].limit").value(80.0))
                .andExpect(jsonPath("$[0].percentage").value(0.0));
    }
}
