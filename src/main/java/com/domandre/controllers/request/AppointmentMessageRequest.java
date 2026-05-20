package com.domandre.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AppointmentMessageRequest {
    @NotBlank(message = "Content is mandatory")
    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String content;
}
