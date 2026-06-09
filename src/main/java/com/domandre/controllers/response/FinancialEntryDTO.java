package com.domandre.controllers.response;

import com.domandre.enums.FinancialEntryStatus;
import com.domandre.enums.FinancialEntryType;
import com.domandre.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FinancialEntryDTO {
    private Long id;
    private FinancialEntryType type;
    private String typeLabel;
    private FinancialEntryStatus status;
    private String statusLabel;
    private BigDecimal amount;
    private String description;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private PaymentMethod paymentMethod;
    private String paymentMethodLabel;
    private LocalDateTime paidAt;
    private Long appointmentId;
    private String patientName;
}
