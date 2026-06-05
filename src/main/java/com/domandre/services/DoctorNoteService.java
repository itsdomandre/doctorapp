package com.domandre.services;

import com.domandre.controllers.request.DoctorNoteRequest;
import com.domandre.entities.Appointment;
import com.domandre.entities.DoctorNote;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.exceptions.AnamnesisRequiredException;
import com.domandre.exceptions.InsufficientPermissionsException;
import com.domandre.repositories.DoctorNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorNoteService {

    private final DoctorNoteRepository doctorNoteRepository;
    private final AppointmentService appointmentService;

    public DoctorNote addNote(Long appointmentId, User doctor, DoctorNoteRequest request) {
        Appointment appointment = appointmentService.getOrThrow(appointmentId);

        if (appointment.getDoctor() == null || !appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new InsufficientPermissionsException();
        }
        if (appointment.getStatus() != AppointmentStatus.APPROVED
                && appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new InsufficientPermissionsException();
        }
        if (appointment.getAnamnesis() == null) {
            throw new AnamnesisRequiredException();
        }

        DoctorNote note = DoctorNote.builder()
                .appointment(appointment)
                .doctor(doctor)
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Doctor {} adding clinical note to appointment {}", doctor.getEmail(), appointmentId);
        return doctorNoteRepository.save(note);
    }
}
