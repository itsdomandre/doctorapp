package com.domandre.services;

import com.domandre.controllers.request.AppointmentRequest;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.repositories.AppointmentRepository;
import com.domandre.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public Appointment createAppointment(AppointmentRequest request) {
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not founded."));
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .appointmentDate(request.getAppointmentDate())
                .notes(request.getNotes())
                .status(AppointmentStatus.REQUESTED)
                .build();
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsByUser() {
        User currentUser = userService.getCurrentUser();
        System.out.println("Fetching appointments for user: " + currentUser.getEmail());
        List<Appointment> appointments = appointmentRepository.findByPatient(currentUser);
        System.out.println("Found appointments: " + appointments.size());
        return appointments;
    }
}
