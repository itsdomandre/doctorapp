package com.domandre.mappers;

import com.domandre.DTOs.AppointmentDTO;
import com.domandre.entities.Appointment;

public class AppointmentMapper {
    public static AppointmentDTO toDTO(Appointment appointment){
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());

        if (appointment.getPatient() != null){
            dto.setPatientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());
        }

        if (appointment.getDoctor() != null){
            dto.setDoctorName(appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName());
        }
        return dto;
    }
}
