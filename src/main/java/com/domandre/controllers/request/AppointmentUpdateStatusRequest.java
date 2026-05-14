package com.domandre.controllers.request;

import com.domandre.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AppointmentUpdateStatusRequest {
    @NotNull
    private AppointmentStatus status;
    private UUID doctorId;
}