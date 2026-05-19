package com.domandre.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "The field newPassword is missing")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).+$",
            message = "Password must contain at least one uppercase letter and one special character"
    )
    private String newPassword;

    @NotBlank(message = "The field confirmNewPassword is missing")
    private String confirmNewPassword;
}