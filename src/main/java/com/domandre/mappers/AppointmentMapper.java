package com.domandre.mappers;

import com.domandre.controllers.response.AppointmentDTO;
import com.domandre.entities.Appointment;

public class AppointmentMapper {
    public static AppointmentDTO toDTO(Appointment appointment){
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setUpdatedAt(appointment.getUpdatedAt());

        if (appointment.getPatient() != null){
            dto.setPatientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());
        }

        if (appointment.getDoctor() != null){
            dto.setDoctorName(appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName());
        }
        return dto;
    }
}
