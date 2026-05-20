package com.domandre.controllers.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppointmentMessageRequest {
    @NotBlank(message = "Content is mandatory")
    private String content;
}
