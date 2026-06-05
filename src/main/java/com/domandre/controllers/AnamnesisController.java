package com.domandre.controllers;

import com.domandre.controllers.request.AnamnesisRequest;
import com.domandre.controllers.response.AnamnesisDTO;
import com.domandre.entities.Anamnesis;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.exceptions.AppointmentNotAprovedException;
import com.domandre.mappers.AnamnesisMapper;
import com.domandre.services.AnamnesisService;
import com.domandre.services.AppointmentService;
import com.domandre.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/anamnesis")
@RequiredArgsConstructor
@Slf4j
public class AnamnesisController {
    private final AppointmentService appointmentService;
    private final AnamnesisService anamnesisService;
    private final UserService userService;

    // ── Admin endpoints ───────────────────────────────────────────────────────

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnamnesisDTO>> getByPatient(@PathVariable UUID patientId) {
        List<AnamnesisDTO> dtoList = anamnesisService.getAnamnesesByPatient(patientId).stream()
                .map(AnamnesisMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/patient/{patientId}/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnamnesisDTO> createAnamnesis(
            @PathVariable UUID patientId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody AnamnesisRequest request) {
        log.info("Creating new anamnesis for patient ID: {}", patientId);
        Anamnesis created = anamnesisService.createAnamnesis(patientId, appointmentId, request);
        log.info("Anamnesis created with ID: {}", created.getId());
        return ResponseEntity.ok(AnamnesisMapper.toDTO(created));
    }

    @PatchMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnamnesisDTO> updateAnamnesisByAppointment(
            @PathVariable Long appointmentId,
            @RequestBody @Valid AnamnesisRequest request) {
        log.info("Updating anamnesis for appointment ID: {}", appointmentId);
        Anamnesis updated = anamnesisService.updateByAppointmentId(appointmentId, request);
        log.info("Anamnesis updated successfully. ID: {}", updated.getId());
        return ResponseEntity.ok(AnamnesisMapper.toDTO(updated));
    }

    @GetMapping("/patient/{patientId}/last")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnamnesisDTO> getLastByPatient(@PathVariable UUID patientId) {
        Anamnesis lastAnamnesis = anamnesisService.getLastByPatient(patientId);
        if (lastAnamnesis == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(AnamnesisMapper.toDTO(lastAnamnesis));
    }

    @GetMapping("/patient/{patientId}/appointment/{appointmentId}/template")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnamnesisDTO> getAnamnesisTemplate(
            @PathVariable UUID patientId,
            @PathVariable long appointmentId) {
        Appointment appointment = appointmentService.getOrThrow(appointmentId);
        if (!AppointmentStatus.APPROVED.equals(appointment.getStatus())) {
            log.warn("Tentativa de template de Anamnesis em appointment não aprovado. ID={}", appointmentId);
            throw new AppointmentNotAprovedException();
        }
        Anamnesis lastAnamnesis = anamnesisService.getLastByPatient(patientId);
        if (lastAnamnesis == null) {
            return ResponseEntity.ok(new AnamnesisDTO());
        }
        return ResponseEntity.ok(AnamnesisMapper.toDTO(lastAnamnesis));
    }

    @GetMapping("/patient/{patientId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnamnesisDTO>> getAnamnesisHistory(
            @PathVariable UUID patientId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        List<AnamnesisDTO> dtos = anamnesisService.getAnamnesisHistory(patientId, startDate, endDate)
                .stream()
                .map(AnamnesisMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // ── Doctor endpoints ──────────────────────────────────────────────────────

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_ADMIN')")
    public ResponseEntity<AnamnesisDTO> getAnamnesisForDoctor(@PathVariable Long appointmentId) {
        User currentUser = userService.getCurrentUser();
        log.info("User {} requesting anamnesis for appointment {}", currentUser.getEmail(), appointmentId);
        Anamnesis anamnesis = anamnesisService.getByAppointmentIdForDoctorOrAdmin(appointmentId, currentUser);
        return ResponseEntity.ok(AnamnesisMapper.toDTO(anamnesis));
    }

    // ── Patient endpoints ─────────────────────────────────────────────────────

    @PostMapping("/my/appointment/{appointmentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AnamnesisDTO> createMyAnamnesis(
            @PathVariable Long appointmentId,
            @Valid @RequestBody AnamnesisRequest request) {
        User patient = userService.getCurrentUser();
        log.info("Patient {} creating anamnesis for appointment {}", patient.getEmail(), appointmentId);
        Anamnesis created = anamnesisService.createForPatient(patient, appointmentId, request);
        return ResponseEntity.ok(AnamnesisMapper.toDTO(created));
    }

    @GetMapping("/my/appointment/{appointmentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AnamnesisDTO> getMyAnamnesis(@PathVariable Long appointmentId) {
        User patient = userService.getCurrentUser();
        Anamnesis anamnesis = anamnesisService.getByAppointmentIdForPatient(appointmentId, patient);
        return ResponseEntity.ok(AnamnesisMapper.toDTO(anamnesis));
    }

    @GetMapping("/my/template")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AnamnesisDTO> getMyTemplate() {
        User patient = userService.getCurrentUser();
        Anamnesis last = anamnesisService.getLastByPatient(patient.getId());
        return ResponseEntity.ok(last != null ? AnamnesisMapper.toDTO(last) : new AnamnesisDTO());
    }
}
