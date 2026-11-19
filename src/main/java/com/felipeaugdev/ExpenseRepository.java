package com.felipeaugdev;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    @EntityGraph(attributePaths = { "category", "recurringExpense" })
    Page<Expense> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = { "category", "recurringExpense" })
    Page<Expense> findByUserIsNull(Pageable pageable);

    @EntityGraph(attributePaths = { "category", "recurringExpense" })
    Page<Expense> findByUserIdAndDateGreaterThanEqual(Long userId, LocalDate from, Pageable pageable);

    @EntityGraph(attributePaths = { "category", "recurringExpense" })
    Page<Expense> findByUserIsNullAndDateGreaterThanEqual(LocalDate from, Pageable pageable);
}
