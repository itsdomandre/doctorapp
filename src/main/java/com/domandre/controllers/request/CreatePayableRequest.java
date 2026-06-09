package com.domandre.controllers.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreatePayableRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 digits and 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
}
