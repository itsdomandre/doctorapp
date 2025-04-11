package com.domandre.controllers.request;

import com.domandre.enums.Procedures;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentRequest {
    private LocalDateTime dateTime;
    private Procedures procedure;
    private String notes;
}
