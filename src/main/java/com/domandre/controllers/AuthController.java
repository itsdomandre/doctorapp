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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
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
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) throws UserAlreadyExistsException {
        log.info("Registration attempt for new user: {}", request.getEmail());
        User user = authService.register(request);
        log.info("Registration completed for user: {}", request.getEmail());
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Login attempt for user: {}", loginRequest.getEmail());
        String token = authService.login(loginRequest);
        log.info("Login successful for user: {}", loginRequest.getEmail() + token);
        ResponseCookie jwtCookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", jwtCookie.toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(name = "token", required = false) String token, HttpServletResponse response) {
        if (token != null) {
            jwtService.invalidateToken(token);
            ResponseCookie deleteCookie = ResponseCookie.from("token", "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Strict")
                    .build();

            response.setHeader("Set-Cookie", deleteCookie.toString());
            log.info("Logout successfully and cookie deleted");
        }

        return ResponseEntity.ok("Logout successfully");
    }

    //TODO: [2]: Implementar confirmation(com token), password reset
    //TODO: [3]: Implementar a invalidação de token, sem necessidade de "store"
}
