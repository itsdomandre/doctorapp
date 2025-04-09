package com.domandre.services;

import com.domandre.controllers.request.AppointmentRequest;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.enums.Role;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.repositories.AppointmentRepository;
import com.domandre.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public Appointment createAppointment(AppointmentRequest request) {
        User patient = UserService.getCurrentUser();

        User doctor = userRepository.findById(request.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));
        if (doctor.getRole() != Role.ADMIN) {
            throw new RuntimeException("Please, select a Doctor");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .appointmentDate(request.getAppointmentDate())
                .patient(patient)
                .doctor(doctor)
                .notes(request.getNotes())
                .status(AppointmentStatus.REQUESTED)
                .build();
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsByUser() {
        User currentUser = UserService.getCurrentUser();
        System.out.println("Fetching appointments for user: " + currentUser.getEmail());
        List<Appointment> appointments = appointmentRepository.findByPatient(currentUser);
        System.out.println("Found appointments: " + appointments.size());
        return appointments;
    }

    public Appointment getOrThrow(Long id) throws ResourceNotFoundException {
        return appointmentRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public List<Appointment> getAppointmentsForCurrentUser(User currentUser) {
        return appointmentRepository.findByPatient(currentUser);
    }

    public Appointment updateAppointmentStatus(Long id, AppointmentStatus newStatus, UUID doctorId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(newStatus);
        if (newStatus == AppointmentStatus.APPROVED) {
            if (doctorId == null) {
                throw new RuntimeException("Doctor ID must be provided");
            }
            User doctor = userRepository.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found."));
            if (doctor.getRole() != Role.ADMIN) {
                throw new RuntimeException("Selected user is not doctor");
            }
            appointment.setDoctor(doctor);
        }
        return appointmentRepository.save(appointment);
    }
}
