package com.domandre.controllers.request;

import com.domandre.enums.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminCreateUserRequest {
    @NotBlank(message = "The field firstName is missing")
    private String firstName;

    @NotBlank(message = "The field lastName is missing")
    private String lastName;

    @NotBlank(message = "The field email is missing")
    @Pattern(
            regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",
            message = "Email must include a valid domain (Ex.: @gmail.com)"
    )
    private String email;

    @NotBlank(message = "The field password is missing")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).+$",
            message = "Password must contain at least one uppercase letter and one special character"
    )
    private String password;

    @NotBlank(message = "The field phoneNumber is missing")
    private String phoneNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "The field birthdate is missing")
    private LocalDate birthdate;

    @NotNull(message = "The field role is missing")
    private Role role;
}
