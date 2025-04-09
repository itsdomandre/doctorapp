package com.domandre.controllers.request;

import com.domandre.enums.AppointmentStatus;
import lombok.Data;

@Data
public class UpdateAppointmentStatusRequest {
    private AppointmentStatus status;
}