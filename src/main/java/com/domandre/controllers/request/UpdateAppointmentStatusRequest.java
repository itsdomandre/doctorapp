package com.domandre.controllers.request;

import com.domandre.enums.AppointmentStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateAppointmentStatusRequest {
    private AppointmentStatus status;
    private UUID doctorId;
}