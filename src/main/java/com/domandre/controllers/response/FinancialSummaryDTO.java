package com.domandre.controllers.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialSummaryDTO {
    private BigDecimal totalReceivablePending;
    private BigDecimal totalReceivablePaid;
    private BigDecimal totalPayablePending;
    private BigDecimal totalPayablePaid;
    private BigDecimal netBalance;
    private long pendingReceivablesCount;
    private long pendingPayablesCount;
    private BigDecimal receivablePaidThisMonth;
    private BigDecimal payablePaidThisMonth;
}
