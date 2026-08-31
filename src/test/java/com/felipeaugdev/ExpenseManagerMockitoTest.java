package com.felipeaugdev;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseManagerMockitoTest {

    @Mock
    private ExpenseRepository repositoryMock;

    @InjectMocks
    private ExpenseService manager;

    @Test
    void testAddExpenseTriggersRepositorySave() {
        when(repositoryMock.loadExpenses()).thenReturn(new ArrayList<>());
        manager = new ExpenseService(repositoryMock);

        manager.addExpense(new BigDecimal("45.00"), "Dinner", "FOOD");

        verify(repositoryMock, times(1)).save(any(Expense.class));
    }

    @Test
    void testDeleteExpenseTriggersRepositoryDelete() {
        Expense sampleExpense = new Expense(1, new BigDecimal("12.00"), LocalDate.now(), "Bus", "TRANSPORTATION");

        when(repositoryMock.loadExpenses()).thenReturn(new ArrayList<>(List.of(sampleExpense)));
        manager = new ExpenseService(repositoryMock);

        boolean deleted = manager.deleteExpense(1);

        assertTrue(deleted);
        verify(repositoryMock, times(1)).deleteById(1);
    }

    @Test
    void testDeleteNonExistentExpenseDoesNotTriggerRepositoryDelete() {
        when(repositoryMock.loadExpenses()).thenReturn(new ArrayList<>());
        manager = new ExpenseService(repositoryMock);

        boolean deleted = manager.deleteExpense(999);

        assertFalse(deleted);
        verify(repositoryMock, never()).deleteById(anyInt());
    }

}
