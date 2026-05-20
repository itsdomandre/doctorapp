package com.domandre.mappers;

import com.domandre.controllers.response.AppointmentMessageDTO;
import com.domandre.entities.AppointmentMessage;

public class AppointmentMessageMapper {

    public static AppointmentMessageDTO toDTO(AppointmentMessage message) {
        AppointmentMessageDTO dto = new AppointmentMessageDTO();
        dto.setId(message.getId());
        dto.setAuthorName(message.getAuthor().getFirstName() + " " + message.getAuthor().getLastName());
        dto.setRole(message.getAuthor().getRole().name());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}
