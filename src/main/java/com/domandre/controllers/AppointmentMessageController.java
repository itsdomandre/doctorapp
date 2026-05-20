package com.domandre.controllers;

import com.domandre.controllers.request.AppointmentMessageRequest;
import com.domandre.controllers.response.AppointmentMessageDTO;
import com.domandre.entities.AppointmentMessage;
import com.domandre.mappers.AppointmentMessageMapper;
import com.domandre.services.AppointmentMessageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointment")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class AppointmentMessageController {

    private final AppointmentMessageService messageService;

    @PostMapping("/{id}/messages")
    public ResponseEntity<AppointmentMessageDTO> addMessage(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentMessageRequest request) {
        log.info("Adding message to appointment ID={}", id);
        AppointmentMessage message = messageService.addMessage(id, request.getContent());
        log.info("Message added to appointment ID={} by author={}", id, message.getAuthor().getEmail());
        return ResponseEntity.ok(AppointmentMessageMapper.toDTO(message));
    }
}
