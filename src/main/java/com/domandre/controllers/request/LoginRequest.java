package com.domandre.controllers.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {
    private String email;
    @NotEmpty(message = "Password cannot be in blank")
    private String password;
}