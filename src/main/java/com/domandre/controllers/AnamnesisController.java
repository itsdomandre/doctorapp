package com.domandre.controllers;

import com.domandre.DTOs.AnamnesisDTO;
import com.domandre.controllers.request.AnamnesisRequest;
import com.domandre.controllers.response.AppointmentDTO;
import com.domandre.entities.Appointment;
import com.domandre.exceptions.NoAppointmentsTodayException;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.mappers.AnamnesisMapper;
import com.domandre.mappers.AppointmentMapper;
import com.domandre.services.AnamnesisService;
import com.domandre.services.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anamnesis")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j

public class AnamnesisController {
    private final AppointmentService appointmentService;
    private final AnamnesisService anamnesisService;

    // TODO: Next step: "Separate" responsabilities of Controller/Services: Appointment <> Anamnesis
    @PostMapping("/{appointmentId}")
    public ResponseEntity<AppointmentDTO> fill(@PathVariable Long appointmentId, @RequestBody AnamnesisRequest request) {
        log.info("Filling anamnesis for appointment ID: {}", appointmentId);
        Appointment updated = appointmentService.fillAnamnesis(appointmentId, request);
        log.info("Anamnesis filled successfully for appointment ID: {}", appointmentId);
        return ResponseEntity.ok(AppointmentMapper.toDTO(updated));
    }

    @GetMapping("/{anamnesisId}")
    public ResponseEntity<AnamnesisDTO> getById(@PathVariable Long appointmentId) throws ResourceNotFoundException, NoAppointmentsTodayException {
        log.info("Retrieving anamnesis for appointment ID: {}", appointmentId);
        Appointment appointment = appointmentService.getOrThrow(appointmentId);
        log.info("Anamnesis fetched for appointment ID: {}", appointmentId);
        return ResponseEntity.ok(AnamnesisMapper.toDTO(appointment.getAnamnesis()));
    }

    @PatchMapping("/{anamnesisId}")
    public ResponseEntity<AnamnesisDTO> update(@PathVariable Long anamnesisId, @RequestBody AnamnesisRequest request) throws ResourceNotFoundException {
        log.info("Updating anamnesis ID: {}", anamnesisId);
        anamnesisService.updateAnamnesis(anamnesisId, request);
        log.info("Anamnesis updated successfully. ID: {}", anamnesisId);
        return ResponseEntity.noContent().build();
    }
}
