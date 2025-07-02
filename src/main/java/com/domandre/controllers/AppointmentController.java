package com.domandre.controllers;

import com.domandre.controllers.request.AppointmentRequest;
import com.domandre.controllers.request.AppointmentSearchRequest;
import com.domandre.controllers.request.UpdateAppointmentStatusRequest;
import com.domandre.controllers.response.AppointmentDTO;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.exceptions.DateTimeRequestIsNotPermittedException;
import com.domandre.exceptions.NoAppointmentsTodayException;
import com.domandre.mappers.AppointmentMapper;
import com.domandre.services.AppointmentService;
import com.domandre.services.UserService;
import com.domandre.validators.AppointmentValidator;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointment")
@SecurityRequirement(name = "bearerAuth")
@Slf4j

public class AppointmentController {
    private final AppointmentService appointmentService;
    private final UserService userService;

    @PostMapping("/create")
    // TODO: - Appointment: Anamnesis - Atrelar ao usuário (PacientID)
    // TODO: - Ao criar Anamnesis -> Retornará o ID da última Anamnesis no momento de preencher
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentRequest request) throws HttpMessageNotReadableException, DateTimeRequestIsNotPermittedException {
        log.info("Creating appointment for date: {}", request.getDateTime());
        LocalDateTime requestTimeToAppointment = request.getDateTime();
        if (!AppointmentValidator.isValidAppointment(requestTimeToAppointment)) {
            throw new DateTimeRequestIsNotPermittedException();
        }

        Appointment appointment = appointmentService.createAppointment(request);
        log.info("Appointment created successfully for date: {}", request.getDateTime());
        return ResponseEntity.ok(AppointmentMapper.toDTO(appointment));
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<List<AppointmentDTO>> getMyAppointments() {
        User currentUser = UserService.getCurrentUser();
        List<Appointment> appointments = appointmentService.getAppointmentsForCurrentUser(currentUser);
        log.info("Found {} appointments for user: {}", appointments.size(), currentUser.getEmail());
        List<AppointmentDTO> dto = appointments.stream()
                .map(AppointmentMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        log.info("Fetching all appointments (ADMIN access)");
        List<Appointment> appointments = appointmentService.getAllAppointments();
        List<AppointmentDTO> dto = appointments.stream()
                .map(AppointmentMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/update/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppointmentDTO> updateAppointment(@PathVariable Long id, @RequestBody UpdateAppointmentStatusRequest request) {
        log.info("Updating appointment status: ID={} | Status={} | Doctor={}", id, request.getStatus(), request.getDoctorId());
        Appointment updated = appointmentService.updateAppointmentStatus(id, request.getStatus(), request.getDoctorId());
        log.info("Appointment status updated successfully. ID={}", updated.getId());
        return ResponseEntity.ok(AppointmentMapper.toDTO(updated));
    }

    @GetMapping("/slots")
    public ResponseEntity<List<String>> getAvailableSlots(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching available slots for date: {}", date);
        List<LocalTime> slots = appointmentService.getAvailableSlots(date);

        List<String> formatted = slots.stream()
                .map(LocalTime::toString)
                .toList();
        log.info("{} available slots found for {}", formatted.size(), date);
        return ResponseEntity.ok(formatted);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentDTO>> getSearchAppointments(AppointmentSearchRequest request) {
        log.info("Searching appointments with filters: {}", request);
        List<Appointment> result = appointmentService.searchAppointments(request);
        log.info("{} appointments matched search criteria", result.size());
        return ResponseEntity.ok(result.stream()
                .map(AppointmentMapper::toDTO)
                .toList());
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentDTO>> getTodayAppointments() throws NoAppointmentsTodayException {
        log.info("Fetching today's appointments");
        List<Appointment> result = appointmentService.getTodayAppointments();

        List<AppointmentDTO> dtoList = new ArrayList<>();
        for (Appointment appointment : result) {
            dtoList.add(AppointmentMapper.toDTO(appointment));
        }
        if (result.isEmpty()) {
            log.warn("No appointments found today");
            throw new NoAppointmentsTodayException();
        }
        log.info("{} appointments found today", dtoList.size());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentDTO>> getPendingAppointments() {
        log.info("Fetching pending appointments");
        List<Appointment> list = appointmentService.getPendingAppointments();
        log.info("{} Pending appointments found", list.size());
        return ResponseEntity.ok(list.stream()
                .map(AppointmentMapper::toDTO)
                .toList());
    }
}
