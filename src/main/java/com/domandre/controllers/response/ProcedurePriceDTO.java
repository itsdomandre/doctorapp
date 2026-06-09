package com.domandre.controllers.response;

import com.domandre.enums.Procedures;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProcedurePriceDTO {
    private Long id;
    private Procedures procedure;
    private String procedureLabel;
    private BigDecimal price;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
