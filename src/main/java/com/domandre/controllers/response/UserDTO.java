package com.domandre.controllers.response;

import com.domandre.enums.Role;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate birthdate;
    private Role role;
}
