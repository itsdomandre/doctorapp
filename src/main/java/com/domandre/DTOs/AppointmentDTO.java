package com.domandre.DTOs;

import com.domandre.enums.AppointmentStatus;
import com.domandre.enums.Procedures;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentDTO {
    private Long id;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private String notes;
    private String patientName;
    private LocalDateTime updatedAt = LocalDateTime.now();
    private String doctorName;
}
