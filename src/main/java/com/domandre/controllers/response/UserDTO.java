package com.domandre.controllers.response;

import com.domandre.enums.Role;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate birthdate;
    private Role role;
}
