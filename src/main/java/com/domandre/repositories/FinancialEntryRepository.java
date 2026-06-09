package com.domandre.repositories;

import com.domandre.entities.FinancialEntry;
import com.domandre.enums.FinancialEntryStatus;
import com.domandre.enums.FinancialEntryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface FinancialEntryRepository extends JpaRepository<FinancialEntry, Long> {

    @Query("SELECT f FROM FinancialEntry f WHERE f.type = :type " +
           "AND (:status IS NULL OR f.status = :status) " +
           "AND (:from IS NULL OR f.dueDate >= :from) " +
           "AND (:to IS NULL OR f.dueDate <= :to)")
    Page<FinancialEntry> findByFilters(
            @Param("type") FinancialEntryType type,
            @Param("status") FinancialEntryStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialEntry f WHERE f.type = :type " +
           "AND (:status IS NULL OR f.status = :status) " +
           "AND (:from IS NULL OR f.dueDate >= :from) " +
           "AND (:to IS NULL OR f.dueDate <= :to)")
    BigDecimal sumAmountByFilters(
            @Param("type") FinancialEntryType type,
            @Param("status") FinancialEntryStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialEntry f WHERE f.type = :type AND f.status = :status")
    BigDecimal sumAmountByTypeAndStatus(@Param("type") FinancialEntryType type, @Param("status") FinancialEntryStatus status);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialEntry f WHERE f.type = :type AND f.status = :status AND f.dueDate BETWEEN :from AND :to")
    BigDecimal sumAmountByTypeAndStatusAndDueDateBetween(
            @Param("type") FinancialEntryType type,
            @Param("status") FinancialEntryStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    long countByTypeAndStatus(FinancialEntryType type, FinancialEntryStatus status);
}
