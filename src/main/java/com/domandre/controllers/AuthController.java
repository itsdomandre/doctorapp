package com.domandre.controllers;

import com.domandre.controllers.request.LoginRequest;
import com.domandre.controllers.request.RegisterRequest;
import com.domandre.controllers.request.ResetPasswordRequest;
import com.domandre.controllers.response.UserDTO;
import com.domandre.entities.User;
import com.domandre.exceptions.InvalidTokenException;
import com.domandre.exceptions.UserAlreadyExistsException;
import com.domandre.mappers.UserMapper;
import com.domandre.services.AuthService;
import com.domandre.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.cookie.secure:false}")
    private boolean appCookieSecure;

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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) throws InvalidTokenException {
        log.info("Login attempt for user: {}", loginRequest.getEmail());
        String token = authService.login(loginRequest);
        log.info("Login successful for user: {}", loginRequest.getEmail() + token);
        ResponseCookie jwtCookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(appCookieSecure)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", jwtCookie.toString());
        // TODO: Temporary token response
        return ResponseEntity.ok(token);
    }

    @PostMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam String token) {
        authService.activateAccount(token);
        return ResponseEntity.ok("Account activated successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        authService.sendPasswordResetToken(email);
        return ResponseEntity.ok("If the email exists, a reset link was sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ResetPasswordRequest body) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Authorization: Bearer <token>");
        }

        String token = authHeader.substring(7);

        if (body.getConfirmNewPassword() != null &&
                !body.getNewPassword().equals(body.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().body("New password and confirm do not match");
        }
        authService.resetPassword(token, body.getNewPassword());
        return ResponseEntity.ok("Password reset successfully");
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<String> resendActivation(@RequestParam String email) {
        authService.resendActivation(email);
        return ResponseEntity.ok("If the account exists and is pending, an activation email was resent");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(appCookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", deleteCookie.toString());
        log.info("Logout successfully");
        return ResponseEntity.ok("Logout successfully");
    }
}