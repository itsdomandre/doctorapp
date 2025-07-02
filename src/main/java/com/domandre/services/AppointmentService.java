package com.domandre.services;

import com.domandre.controllers.request.AppointmentRequest;
import com.domandre.controllers.request.AppointmentSearchRequest;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.enums.Role;
import com.domandre.exceptions.DateTimeRequestIsNotPermittedException;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.helpers.BusinessHoursHelper;
import com.domandre.helpers.BusinessHoursHelper.TimeRange;
import com.domandre.repositories.AnamnesisRepository;
import com.domandre.repositories.AppointmentRepository;
import com.domandre.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AnamnesisRepository anamnesisRepository;

    public Appointment createAppointment(AppointmentRequest request) throws DateTimeRequestIsNotPermittedException {
        User patient = UserService.getCurrentUser();
        boolean existsAppointment = appointmentRepository.existsByAppointmentDate(request.getDateTime());
        if (existsAppointment == true) {
            throw new DateTimeRequestIsNotPermittedException();
        }
        Appointment appointment = Appointment.builder().
                patient(patient)
                .appointmentDate(request.getDateTime())
                .procedure(request.getProcedure())
                .notes(request.getNotes()).status(AppointmentStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByUser() {
        User currentUser = UserService.getCurrentUser();
        System.out.println("Fetching appointments for user: " + currentUser.getEmail());
        List<Appointment> appointments = appointmentRepository.findByPatient(currentUser);
        System.out.println("Found appointments: " + appointments.size());
        return appointments;
    }

    public Appointment getOrThrow(Long id) throws ResourceNotFoundException {
        return appointmentRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Appointment> getAppointmentsForCurrentUser(User currentUser) {
        return appointmentRepository.findByPatient(currentUser);
    }

    public Appointment updateAppointmentStatus(Long id, AppointmentStatus newStatus, UUID doctorId) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));

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
            appointment.setUpdatedAt(LocalDateTime.now());
        }
        return appointmentRepository.save(appointment);
    }

    public List<LocalTime> getAvailableSlots(LocalDate date) {
        Optional<TimeRange> rangeOptional = BusinessHoursHelper.getBusinessHours(date.getDayOfWeek());
        if (rangeOptional.isEmpty()) {
            return List.of();
        }
        TimeRange range = rangeOptional.get();
        LocalTime start = range.start();
        LocalTime end = range.end().minusHours(1);

        List<LocalTime> allPossibleSlots = new ArrayList<>();
        for (LocalTime time = start; !time.isAfter(end); time = time.plusHours(1)) {
            allPossibleSlots.add(time);
        }
        List<LocalDateTime> taken = appointmentRepository.findAllByAppointmentDateBetween(date.atStartOfDay(), date.atTime(23, 59)).stream().map(Appointment::getAppointmentDate).toList();
        // Filtra os horários disponíveis (que ainda não foram agendados)
        return allPossibleSlots.stream().filter(slot -> {
            LocalDateTime dateTime = LocalDateTime.of(date, slot);

            // Não pode ser um horário passado
            if (dateTime.isBefore(LocalDateTime.now())) return false;

            // Não pode ser um horário já agendado
            boolean alreadyTaken = taken.stream().anyMatch(t -> t.toLocalTime().equals(slot));

            return !alreadyTaken;
        }).collect(Collectors.toList());
    }

    public List<Appointment> searchAppointments(AppointmentSearchRequest request) {
        LocalDate from = request.getFromDate() != null ? request.getFromDate() : LocalDate.now().minusMonths(1);
        LocalDate to = request.getToDate() != null ? request.getToDate() : LocalDate.now().plusMonths(1);

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(23, 59);

        List<Appointment> base = appointmentRepository.findAllByAppointmentDateBetween(fromDateTime, toDateTime);

        return base.stream().filter(appointment -> request.getPatientId() == null || appointment.getPatient().getId().equals(request.getPatientId())).filter(appointment -> request.getStatus() == null || appointment.getStatus() == request.getStatus()).filter(appointment -> request.getPatientName() == null || appointment.getPatient().getFirstName().toLowerCase().contains(request.getPatientName().toLowerCase()) || appointment.getPatient().getLastName().toLowerCase().contains(request.getPatientName().toLowerCase())).toList();
    }

    public List<Appointment> getTodayAppointments() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(23, 59);
        return appointmentRepository.findAllByAppointmentDateBetween(from, to);
        // DateBetween must be 2 arguments (from, to) JPA+DB characteristic
    }

    public List<Appointment> getPendingAppointments() {
        return appointmentRepository.findAllRequested();
    }
}
