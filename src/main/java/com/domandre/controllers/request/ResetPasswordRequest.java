package com.domandre.controllers.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmNewPassword;
}