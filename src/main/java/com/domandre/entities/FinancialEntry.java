package com.domandre.entities;

import com.domandre.enums.FinancialEntryStatus;
import com.domandre.enums.FinancialEntryType;
import com.domandre.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "financial_entries")
public class FinancialEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private FinancialEntryType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialEntryStatus status;

    @Column(nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDate dueDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", updatable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", updatable = false)
    private User patient;
}
