package com.domandre.controllers;

import com.domandre.DTOs.AnamnesisDTO;
import com.domandre.controllers.request.AnamnesisRequest;
import com.domandre.entities.Anamnesis;
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
        List<Anamnesis> anamneses = anamnesisService.getAnamnesesByPatient(patientId);
        List<AnamnesisDTO> dtoList = anamneses.stream()
                .map(AnamnesisMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnamnesisDTO> createAnamnesis(@PathVariable UUID patientId, @Valid @RequestBody AnamnesisRequest request) throws ResourceNotFoundException {
        log.info("Creating new anamnesis for patient ID: {}", patientId);
        Anamnesis created = anamnesisService.createAnamnesis(patientId, request);
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
        return ResponseEntity.ok(AnamnesisMapper.toDTO(updated));
    }
}
