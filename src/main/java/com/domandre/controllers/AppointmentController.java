package com.domandre.controllers;

import com.domandre.controllers.request.AppointmentRequest;
import com.domandre.controllers.request.UpdateAppointmentStatusRequest;
import com.domandre.controllers.response.AppointmentDTO;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.mappers.AppointmentMapper;
import com.domandre.services.AppointmentService;
import com.domandre.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import validators.AppointmentValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointment")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody AppointmentRequest request) {
        LocalDateTime requestTimeToAppointment = request.getDateTime();
        if (!AppointmentValidator.isValidAppointment(requestTimeToAppointment)) {
            throw new IllegalArgumentException("Date/hour invalid. Check the available schedule");
        }

        Appointment appointment = appointmentService.createAppointment(request);
        return ResponseEntity.ok(AppointmentMapper.toDTO(appointment));
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<List<AppointmentDTO>> getMyAppointments() {
        User currentUser = UserService.getCurrentUser();
        List<Appointment> appointments = appointmentService.getAppointmentsForCurrentUser(currentUser);

        List<AppointmentDTO> dto = appointments.stream()
                .map(AppointmentMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentDTO>> getAll() {
        List<Appointment> appointments = appointmentService.getAllAppointments();

        List<AppointmentDTO> dto = appointments.stream()
                .map(AppointmentMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/update/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppointmentDTO> updateAppointment(@PathVariable Long id, @RequestBody UpdateAppointmentStatusRequest request) {
        Appointment updated = appointmentService.updateAppointmentStatus(id, request.getStatus(), request.getDoctorId());
        return ResponseEntity.ok(AppointmentMapper.toDTO(updated));
    }

    @GetMapping("/slots")
    public ResponseEntity<List<String>> getAvailableSlots(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<LocalTime> slots = appointmentService.getAvailableSlots(date);

        List<String> formatted = slots.stream()
                .map(LocalTime::toString)
                .toList();

        return ResponseEntity.ok(formatted);
    }

}
