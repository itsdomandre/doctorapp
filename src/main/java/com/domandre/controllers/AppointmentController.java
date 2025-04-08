package com.domandre.controllers;

import com.domandre.controllers.request.AppointmentRequest;
import com.domandre.entities.Appointment;
import com.domandre.services.AppointmentService;
import com.domandre.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointment")

public class AppointmentController {
    private final AppointmentService appointmentService;
    private final UserService userService;

    @GetMapping("/myAppointments")
    public ResponseEntity<List<Appointment>> getMyAppointments() {
        List<Appointment> appointments = appointmentService.getAppointmentsByUser();
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request) {
        Appointment appointment = appointmentService.createAppointment(request);
        return ResponseEntity.ok(appointment);
    }
}
