package com.felipeaugdev.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.fasterxml.jackson.databind.JsonNode;

@SuppressWarnings("null")
class ErrorHandlingIntegrationTest extends AbstractIntegrationTest {

    @Test
    void errorsShareOneJsonShape() throws Exception {
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("category", "FOOD", "amount", 100.00))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("You must be logged in to set a budget"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void validationErrorsListTheBadFields() throws Exception {
        String alice = registerAndLogin("alice");

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + alice)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("amount", -5, "description", "Oops", "category", "FOOD"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    void cannotUpdateAnotherUsersExpense() throws Exception {
        String alice = registerAndLogin("alice");
        String bob = registerAndLogin("bob");
        JsonNode expense = createExpense(alice, "Alice lunch", "FOOD", 12.50);

        mockMvc.perform(put("/api/expenses/" + expense.get("id").asInt())
                .header("Authorization", "Bearer " + bob)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "amount", 1.00,
                        "date", LocalDate.now().toString(),
                        "description", "Hacked",
                        "category", "FOOD"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Expense not found"));

        getExpenses(alice, "")
                .andExpect(jsonPath("$.content[0].description").value("Alice lunch"));
    }

    @Test
    void unknownCategoryIsABadRequestWhenCreatingAnExpense() throws Exception {
        String alice = registerAndLogin("alice");

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + alice)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("amount", 5.00, "description", "Mystery", "category", "NOPE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category not found: NOPE"));
    }

    @Test
    void unknownCategoryIsABadRequestWhenUpdatingAnExpense() throws Exception {
        String alice = registerAndLogin("alice");
        JsonNode expense = createExpense(alice, "Lunch", "FOOD", 10.00);

        mockMvc.perform(put("/api/expenses/" + expense.get("id").asInt())
                .header("Authorization", "Bearer " + alice)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "amount", 10.00,
                        "date", LocalDate.now().toString(),
                        "description", "Lunch",
                        "category", "NOPE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category not found: NOPE"));
    }

    @Test
    void otherUsersCustomCategoriesAreInvisible() throws Exception {
        String alice = registerAndLogin("alice");
        String bob = registerAndLogin("bob");
        createCategory(alice, "Gym").andExpect(status().isCreated());

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + bob)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("amount", 5.00, "description", "Sneaky", "category", "GYM"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category not found: GYM"));
    }

    @Test
    void deletingSomeoneElsesBudgetLooksLikeNotFound() throws Exception {
        String alice = registerAndLogin("alice");
        String bob = registerAndLogin("bob");

        String response = mockMvc.perform(post("/api/budgets")
                .header("Authorization", "Bearer " + alice)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("category", "FOOD", "amount", 100.00))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int budgetId = objectMapper.readTree(response).get("id").asInt();

        mockMvc.perform(delete("/api/budgets/" + budgetId).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Budget not found"));
        mockMvc.perform(delete("/api/budgets/" + budgetId).header("Authorization", "Bearer " + alice))
                .andExpect(status().isNoContent());
    }

    @Test
    void guestsCannotDeleteBudgets() throws Exception {
        mockMvc.perform(delete("/api/budgets/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("You must be logged in to delete a budget"));
    }

    @Test
    void emptyCsvImportIsABadRequest() throws Exception {
        String alice = registerAndLogin("alice");
        MockMultipartFile empty = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/expenses/import").file(empty)
                .header("Authorization", "Bearer " + alice))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File is empty"));
    }
}
