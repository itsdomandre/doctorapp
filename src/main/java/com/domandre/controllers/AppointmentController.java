package com.domandre.controllers;

import com.domandre.controllers.request.AppointmentRequest;
import com.domandre.controllers.request.AppointmentSearchRequest;
import com.domandre.controllers.request.AppointmentUpdateStatusRequest;
import com.domandre.controllers.request.DoctorNoteRequest;
import com.domandre.controllers.response.AppointmentDTO;
import com.domandre.controllers.response.DoctorNoteDTO;
import com.domandre.entities.Appointment;
import com.domandre.entities.DoctorNote;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.exceptions.*;
import org.springframework.data.domain.Page;
import com.domandre.mappers.AppointmentMapper;
import com.domandre.mappers.DoctorNoteMapper;
import com.domandre.services.AppointmentService;
import com.domandre.services.DoctorNoteService;
import com.domandre.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointment")
@SecurityRequirement(name = "bearerAuth")
@Slf4j

public class AppointmentController {
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final DoctorNoteService doctorNoteService;

    @PostMapping
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        log.info("Creating appointment for date: {}", request.getDateTime());
        Appointment appointment = appointmentService.createAppointment(request);
        log.info("Appointment created successfully for date: {}", request.getDateTime());
        return ResponseEntity.ok(AppointmentMapper.toDTO(appointment));
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<Page<AppointmentDTO>> getMyAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AppointmentStatus status) {
        User currentUser = userService.getCurrentUser();
        Page<AppointmentDTO> result = appointmentService
                .getAppointmentsForCurrentUser(currentUser, status, page, size)
                .map(AppointmentMapper::toDTO);
        log.info("Found {} appointments for user: {}", result.getTotalElements(), currentUser.getEmail());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AppointmentDTO>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching all appointments (ADMIN access), page={}, size={}", page, size);
        return ResponseEntity.ok(appointmentService.getAllAppointments(page, size).map(AppointmentMapper::toDTO));
    }

    @PutMapping("/update/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppointmentDTO> updateAppointment(@PathVariable Long id, @RequestBody AppointmentUpdateStatusRequest request) {
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
    public ResponseEntity<Page<AppointmentDTO>> getTodayAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching today's appointments, page={}, size={}", page, size);
        Page<AppointmentDTO> result = appointmentService.getTodayAppointments(page, size).map(AppointmentMapper::toDTO);
        log.info("{} appointments found today", result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentDTO> cancelAppointment(@PathVariable Long id)
    {
        log.info("Cancelling appointment ID={}", id);
        Appointment cancelled = appointmentService.cancelAppointment(id);
        log.info("Appointment ID={} cancelled successfully", id);
        return ResponseEntity.ok(AppointmentMapper.toDTO(cancelled));
    }

    @GetMapping("/monthly-availability")
    public ResponseEntity<Map<String, Long>> getMonthlyAvailability(@RequestParam("month") String month) {
        log.info("Fetching monthly availability for: {}", month);
        return ResponseEntity.ok(appointmentService.getMonthlyAvailability(YearMonth.parse(month)));
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

    @GetMapping("/doctor/my-appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<AppointmentDTO>> getDoctorAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AppointmentStatus status) {
        User currentDoctor = userService.getCurrentUser();
        Page<AppointmentDTO> result = appointmentService
                .getAppointmentsForCurrentDoctor(currentDoctor, status, page, size)
                .map(AppointmentMapper::toDTO);
        log.info("Found {} appointments for doctor: {}", result.getTotalElements(), currentDoctor.getEmail());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/report/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AppointmentDTO>> getPatientHistory(
            @PathVariable java.util.UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("ADMIN requesting appointment history for patientId={} | from={} | to={} | status={}", patientId, from, to, status);
        Page<AppointmentDTO> result = appointmentService
                .getPatientAppointmentHistory(patientId, from, to, status, page, size)
                .map(AppointmentMapper::toDTO);
        log.info("Patient history: {} total records for patientId={}", result.getTotalElements(), patientId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        log.info("User {} fetching appointment ID={}", currentUser.getEmail(), id);
        Appointment appointment = appointmentService.getAppointmentForUser(id, currentUser);
        return ResponseEntity.ok(AppointmentMapper.toDTO(appointment));
    }

    @PostMapping("/{id}/doctor-notes")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorNoteDTO> addDoctorNote(
            @PathVariable Long id,
            @Valid @RequestBody DoctorNoteRequest request) {
        User doctor = userService.getCurrentUser();
        log.info("Doctor {} adding note to appointment ID={}", doctor.getEmail(), id);
        DoctorNote note = doctorNoteService.addNote(id, doctor, request);
        return ResponseEntity.ok(DoctorNoteMapper.toDTO(note));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentDTO> completeAppointment(@PathVariable Long id) {
        User currentDoctor = userService.getCurrentUser();
        log.info("Doctor {} marking appointment ID={} as COMPLETED", currentDoctor.getEmail(), id);
        Appointment completed = appointmentService.completeAppointment(id, currentDoctor);
        log.info("Appointment ID={} marked as COMPLETED", id);
        return ResponseEntity.ok(AppointmentMapper.toDTO(completed));
    }
}
