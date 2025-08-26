package com.domandre.mappers;

import com.domandre.controllers.response.UserDTO;
import com.domandre.entities.User;

public class UserMapper {
    public static UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setFullName(user.getFirstName() + " " + user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setBirthdate(user.getBirthdate());
        dto.setRole(user.getRole());
        return dto;
    }
}