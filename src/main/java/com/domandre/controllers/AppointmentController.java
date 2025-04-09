package com.domandre.controllers;

import com.domandre.controllers.request.AppointmentRequest;
import com.domandre.controllers.request.UpdateAppointmentStatusRequest;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.services.AppointmentService;
import com.domandre.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointment")

public class AppointmentController {
    private final AppointmentService appointmentService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request) {
        Appointment appointment = appointmentService.createAppointment(request);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<List<Appointment>> getMyAppointments() {
        User currentUser = UserService.getCurrentUser();
        List<Appointment> appointments = appointmentService.getAppointmentsForCurrentUser(currentUser);
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/update/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody UpdateAppointmentStatusRequest request) {
        Appointment updated = appointmentService.updateAppointmentStatus(id, request.getStatus());
        return ResponseEntity.noContent().build();
    }


}
