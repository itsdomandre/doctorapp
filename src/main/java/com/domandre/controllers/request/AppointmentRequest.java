package com.domandre.controllers.request;

import com.domandre.enums.Procedures;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    @NotNull(message = "Date/hour is Mandatory")
    private LocalDateTime dateTime;
    @NotNull(message = "Procedure is Mandatory")
    private Procedures procedure;
    private String notes;
}
