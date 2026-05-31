package com.domandre.controllers;

import com.domandre.controllers.request.LoginRequest;
import com.domandre.controllers.request.RegisterRequest;
import com.domandre.controllers.request.ResetPasswordRequest;
import com.domandre.controllers.response.UserDTO;
import com.domandre.entities.User;
import com.domandre.mappers.UserMapper;
import com.domandre.exceptions.InvalidTokenException;
import com.domandre.exceptions.PasswordMismatchException;
import com.domandre.services.AuthService;
import com.domandre.services.JwtService;
import com.domandre.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtService jwtService;
    private final UserService userService;

    @Value("${app.cookie.secure:false}")
    private boolean appCookieSecure;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getLoggedUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for new user: {}", request.getEmail());
        User user = authService.register(request);
        log.info("Registration completed for user: {}", request.getEmail());
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Login attempt for user: {}", loginRequest.getEmail());
        String token = authService.login(loginRequest);
        log.info("Login successful for user: {}", loginRequest.getEmail());
        ResponseCookie jwtCookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(appCookieSecure)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", jwtCookie.toString());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestParam("token") String token) {
        authService.activateAccount(token);
        return ResponseEntity.ok().build();
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
            throw new InvalidTokenException();
        }

        String token = authHeader.substring(7);

        if (!body.getNewPassword().equals(body.getConfirmNewPassword())) {
            throw new PasswordMismatchException();
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
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String jwt = jwtService.resolveToken(request);
        if (jwt != null) {
            String userEmail = jwtService.getUsernameFromJWT(jwt);
            authService.logout(jwt);
            log.info("Logout successful for user: {}", userEmail);
        }
        ResponseCookie deleteCookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(appCookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", deleteCookie.toString());
        return ResponseEntity.ok("Logout successfully");
    }
}