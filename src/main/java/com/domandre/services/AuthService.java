package com.domandre.services;

import com.domandre.controllers.request.LoginRequest;
import com.domandre.controllers.request.RegisterRequest;
import com.domandre.entities.User;
import com.domandre.enums.Role;
import com.domandre.exceptions.UserAlreadyExistsException;
import com.domandre.repositories.InvalidTokenRepository;
import com.domandre.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService tokenProvider;
    private final InvalidTokenRepository invalidTokenRepository;
    private final MailService mailService;

    public User register(RegisterRequest request) throws UserAlreadyExistsException {
        log.info("Verifying if the user exists...");
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException();
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBirhdate(request.getBirthdate());
        Role role = request.getRole() != null ? request.getRole() : Role.USER;
        user.setRole(role);
        //mailService.sendWelcomeEmail(requestedNewUser.getEmail(), requestedNewUser.getFirstName());

        return userRepository.save(user);
    }

    public String login(LoginRequest loginRequest) throws BadCredentialsException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        return tokenProvider.generateToken(authentication);
    }

    public boolean isTokenBlacklisted(String token) {
        log.info("Token invalidated successfully");
        return invalidTokenRepository.existsByToken(token);
    }
}

