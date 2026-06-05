package com.domandre.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorNoteRequest {

    @NotBlank
    @Size(max = 1000)
    private String content;
}
