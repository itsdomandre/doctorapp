package com.domandre.controllers;

import com.domandre.DTOs.AnamnesisDTO;
import com.domandre.controllers.request.AnamnesisRequest;
import com.domandre.controllers.response.AppointmentDTO;
import com.domandre.entities.Appointment;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.mappers.AnamnesisMapper;
import com.domandre.mappers.AppointmentMapper;
import com.domandre.services.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anamnesis")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor

public class AnamnesisController {
    private final AppointmentService appointmentService;

    @PostMapping("/{appointmentId}")
    public ResponseEntity<AppointmentDTO> fill(@PathVariable Long appointmentId, @RequestBody AnamnesisRequest request) {
        Appointment updated = appointmentService.fillAnamnesis(appointmentId, request);
        return ResponseEntity.ok(AppointmentMapper.toDTO(updated));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AnamnesisDTO> getById(@PathVariable Long appointmentId) throws ResourceNotFoundException {
        Appointment appointment = appointmentService.getOrThrow(appointmentId);
        return ResponseEntity.ok(AnamnesisMapper.toDTO(appointment.getAnamnesis()));
    }
}
