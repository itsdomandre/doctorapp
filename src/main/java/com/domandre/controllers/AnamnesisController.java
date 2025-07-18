package com.domandre.controllers;

import com.domandre.controllers.request.AnamnesisRequest;
import com.domandre.controllers.response.AnamnesisDTO;
import com.domandre.entities.Anamnesis;
import com.domandre.entities.Appointment;
import com.domandre.enums.AppointmentStatus;
import com.domandre.exceptions.AnamnesisAlreadyExistsException;
import com.domandre.exceptions.AppointmentNotAprovedException;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.mappers.AnamnesisMapper;
import com.domandre.services.AnamnesisService;
import com.domandre.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/anamnesis")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j

public class AnamnesisController {
    private final AppointmentService appointmentService;
    private final AnamnesisService anamnesisService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnamnesisDTO>> getByPatient(@PathVariable UUID patientId) {
        List<Anamnesis> anamnesis = anamnesisService.getAnamnesesByPatient(patientId);
        List<AnamnesisDTO> dtoList = anamnesis.stream()
                .map(AnamnesisMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/patient/{patientId}/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnamnesisDTO> createAnamnesis(@PathVariable UUID patientId, @PathVariable Long appointmentId, @Valid @RequestBody AnamnesisRequest request) throws ResourceNotFoundException, AppointmentNotAprovedException, AnamnesisAlreadyExistsException {
        log.info("Creating new anamnesis for patient ID: {}", patientId);
        Anamnesis created = anamnesisService.createAnamnesis(patientId, appointmentId, request);
        log.info("Anamnesis created with ID: {}", created.getId());
        return ResponseEntity.ok(AnamnesisMapper.toDTO(created));
    }

    @PatchMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnamnesisDTO> updateAnamnesisByAppointment(@PathVariable Long appointmentId, @RequestBody @Valid AnamnesisRequest request
    ) throws ResourceNotFoundException {
        log.info("Updating anamnesis for appointment ID: {}", appointmentId);
        Anamnesis updated = anamnesisService.updateByAppointmentId(appointmentId, request);
        log.info("Anamnesis updated successfully. ID: {}", updated.getId());
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<AnamnesisDTO> getAnamnesisTemplate(@PathVariable UUID patientId, @PathVariable long appointmentId) throws ResourceNotFoundException, AppointmentNotAprovedException {
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
    public ResponseEntity<List<AnamnesisDTO>> getAnamnesisHistory(@PathVariable UUID patientId, @RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate) {
        List<Anamnesis> anamneses = anamnesisService.getAnamnesesByPatient(patientId);
        List<AnamnesisDTO> dtos = new ArrayList<>();

        for (Anamnesis a : anamneses) {
            if (a.getCreatedAt() == null) continue;
            LocalDate createdDate = a.getCreatedAt().toLocalDate();
            if (startDate != null && createdDate.isBefore(startDate)) continue;
            if (endDate != null && createdDate.isAfter(startDate)) continue;
            dtos.add(AnamnesisMapper.toDTO(a));
        }
        return ResponseEntity.ok(dtos);
    }
}
