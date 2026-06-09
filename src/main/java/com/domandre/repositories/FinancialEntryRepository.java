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

    Page<FinancialEntry> findByType(FinancialEntryType type, Pageable pageable);

    Page<FinancialEntry> findByTypeAndStatus(FinancialEntryType type, FinancialEntryStatus status, Pageable pageable);

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
