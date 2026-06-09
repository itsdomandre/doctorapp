package com.domandre.controllers.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialEntryPageResponse {
    private List<FinancialEntryDTO> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;
    private BigDecimal filteredTotal;
}
