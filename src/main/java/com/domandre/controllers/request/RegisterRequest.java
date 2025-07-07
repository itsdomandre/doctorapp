package com.domandre.controllers.request;

import com.domandre.enums.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
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
    private String password;
    @NotBlank(message = "The field phoneNumber is missing")
    private String phoneNumber;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "The field Birthdate is missing")
    private LocalDate birthdate;
    private Role role;

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
