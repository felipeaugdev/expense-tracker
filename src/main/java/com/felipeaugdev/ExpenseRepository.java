package com.felipeaugdev;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            SELECT e.category.name AS name, SUM(e.amount) AS total
            FROM Expense e
            WHERE e.user.id = :userId AND e.date BETWEEN :from AND :to
            GROUP BY e.category.name
            """)
    List<CategoryTotal> sumByCategoryForUser(@Param("userId") Long userId,
            @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT e.category.name AS name, SUM(e.amount) AS total
            FROM Expense e
            WHERE e.user IS NULL AND e.date BETWEEN :from AND :to
            GROUP BY e.category.name
            """)
    List<CategoryTotal> sumByCategoryForGuests(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT SUM(e.amount) FROM Expense e
            WHERE e.user.id = :userId AND e.date BETWEEN :from AND :to
            """)
    BigDecimal sumForUser(@Param("userId") Long userId,
            @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT SUM(e.amount) FROM Expense e
            WHERE e.user IS NULL AND e.date BETWEEN :from AND :to
            """)
    BigDecimal sumForGuests(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
