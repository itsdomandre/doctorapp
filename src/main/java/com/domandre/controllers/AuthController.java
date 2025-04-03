package com.domandre.controllers;

import com.domandre.config.JwtTokenProvider;
import com.domandre.controllers.request.LoginRequest;
import com.domandre.controllers.request.RegisterRequest;
import com.domandre.entities.User;
import com.domandre.exceptions.UserAlreadyExistsException;
import com.domandre.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")

public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) throws UserAlreadyExistsException {
        User user = authService.register(request);
        return ResponseEntity.ok("User " + "» " + request.getEmail() + " «" + " created successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest);
        return ResponseEntity.ok("User: " + loginRequest.getEmail() + " logged succesfully! \n\nToken: " + token);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        jwtTokenProvider.invalidateToken(token);
        return ResponseEntity.ok("Logout successfully");
    }

    //TODO [2]: Implementar confirmation(com token), password reset
}
