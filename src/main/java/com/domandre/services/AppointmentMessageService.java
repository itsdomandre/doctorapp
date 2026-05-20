package com.domandre.services;

import com.domandre.entities.Appointment;
import com.domandre.entities.AppointmentMessage;
import com.domandre.entities.User;
import com.domandre.enums.Role;
import com.domandre.exceptions.InsufficientPermissionsException;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.repositories.AppointmentMessageRepository;
import com.domandre.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppointmentMessageService {

    private final AppointmentMessageRepository messageRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;

    public AppointmentMessage addMessage(Long appointmentId, String content) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(ResourceNotFoundException::new);
        User author = userService.getCurrentUser();

        if (author.getRole() != Role.ADMIN && !appointment.getPatient().getId().equals(author.getId())) {
            throw new InsufficientPermissionsException();
        }

        AppointmentMessage message = AppointmentMessage.builder()
                .appointment(appointment)
                .author(author)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }
}
