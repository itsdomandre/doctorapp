package com.domandre.controllers;

import com.domandre.controllers.request.UpdateRoleRequest;
import com.domandre.controllers.response.UserDTO;
import java.util.UUID;
import com.domandre.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        log.info("Admin updating role of user {} to {}", id, request.getRole());
        return ResponseEntity.ok(userService.updateUserRole(id, request.getRole()));
    }

    @GetMapping("/all-patients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all patients");
        return ResponseEntity.ok(userService.getAllPatients(page, size));
    }
}
