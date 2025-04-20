package com.domandre.controllers;

import com.domandre.controllers.request.LoginRequest;
import com.domandre.controllers.request.RegisterRequest;
import com.domandre.controllers.response.UserDTO;
import com.domandre.entities.User;
import com.domandre.exceptions.UserAlreadyExistsException;
import com.domandre.mappers.UserMapper;
import com.domandre.services.AuthService;
import com.domandre.services.JwtService;
import com.domandre.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j

public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated")
    public ResponseEntity<UserDTO> getLoggedUser() {
        User user = UserService.getCurrentUser();
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest request) throws UserAlreadyExistsException {
        log.info("Registration attempt for new user: {}", request.getEmail());
        User user = authService.register(request);
        log.info("Registration completed for user: {}", request.getEmail());
        return ResponseEntity.ok(UserMapper.toDTO(user));

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getEmail());
        String token = authService.login(loginRequest);
        log.info("Login successful for user: {}", loginRequest.getEmail());
        return ResponseEntity.ok("User: " + loginRequest.getEmail() + " logged succesfully! \n\nToken: " + token);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        jwtService.invalidateToken(token);
        log.info("Logout successfully");
        return ResponseEntity.ok("Logout successfully");
    }

    //TODO [2]: Implementar confirmation(com token), password reset
}
