package com.domandre.controllers.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentRequest {
    private LocalDateTime appointmentDate;
    private String notes;
    private UUID doctorId;
}
