package com.domandre.controllers.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentRequest {
    private UUID patientId;
    private LocalDateTime appointmentDate;
    private String notes;
}
