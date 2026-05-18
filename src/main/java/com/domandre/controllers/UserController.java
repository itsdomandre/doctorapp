package com.domandre.controllers;

import com.domandre.controllers.request.RegisterRequest;
import com.domandre.controllers.response.UserDTO;
import com.domandre.entities.User;
import com.domandre.mappers.UserMapper;
import com.domandre.services.AuthService;
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
    private final AuthService authService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info("Admin creating new user: {} with role: {}", request.getEmail(), request.getRole());
        User user = authService.register(request);
        return ResponseEntity.ok(UserMapper.toDTO(user));
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
