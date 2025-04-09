package com.domandre.controllers.request;

import com.domandre.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @Email(message = "Invalid email")
    private String email;
    @NotBlank(message = "The field password is missing")
    private String password;
    @NotBlank(message = "The field phoneNumber is missing")
    private String phoneNumber;

    @DateTimeFormat(pattern = "yyyyMMdd")
    private LocalDate birthdate;
    private Role role;

    //TODO Printar campos sensíveis de forma a seguir os padrões da LGPD
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
