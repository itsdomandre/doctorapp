package com.domandre.mappers;

import com.domandre.controllers.response.AppointmentDTO;
import com.domandre.entities.Appointment;

import java.util.List;
import java.util.stream.Collectors;

public class AppointmentMapper {
    public static AppointmentDTO toDTO(Appointment appointment){
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setDateTime(appointment.getAppointmentDate());
        dto.setProcedure(appointment.getProcedure());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setUpdatedAt(appointment.getUpdatedAt());

        if (appointment.getPatient() != null){
            dto.setPatientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());
        }

        if (appointment.getDoctor() != null){
            dto.setDoctorName(appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName());
        }
        dto.setMessages(appointment.getMessages().stream()
                .map(AppointmentMessageMapper::toDTO)
                .collect(Collectors.toList()));
        if (appointment.getAnamnesis() != null) {
            dto.setAnamnesisId(appointment.getAnamnesis().getId());
        }
        if (appointment.getDoctorNotes() != null && !appointment.getDoctorNotes().isEmpty()) {
            dto.setDoctorNotes(appointment.getDoctorNotes().stream()
                    .map(DoctorNoteMapper::toDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
