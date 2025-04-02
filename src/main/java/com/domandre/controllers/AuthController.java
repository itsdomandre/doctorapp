package com.domandre.controllers;

import com.domandre.controllers.request.RegisterRequest;
import com.domandre.entities.User;
import com.domandre.exceptions.UserAlreadyExistsException;
import com.domandre.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")

public class AuthController {
    //TODO [1]: Implementar register(signup), login, logout
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) throws UserAlreadyExistsException {
        User user = authService.register(request);
        return ResponseEntity.ok("User " + "» " + request.getEmail() + " «" + " created successfully!");
    }
    //TODO [2]: Implementar confirmation(com token), password reset
}
