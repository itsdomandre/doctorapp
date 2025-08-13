package com.domandre.services;

import com.domandre.controllers.request.LoginRequest;
import com.domandre.controllers.request.RegisterRequest;
import com.domandre.entities.User;
import com.domandre.enums.Role;
import com.domandre.enums.UserStatus;
import com.domandre.exceptions.InvalidTokenException;
import com.domandre.exceptions.UserAlreadyExistsException;
import com.domandre.repositories.InvalidTokenRepository;
import com.domandre.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final JwtService jwtService;
    private final InvalidTokenRepository invalidTokenRepository;
    private final MailService mailService;

    @Value("${app.log.tokens:false}")
    private boolean logTokens;

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
        user.setBirthdate(request.getBirthdate());
        Role role = request.getRole() != null ? request.getRole() : Role.USER;
        user.setRole(role);
        user.setStatus(UserStatus.UNVERIFIED);

        userRepository.save(user);
        String activationToken = jwtService.generateTokenToActivatonOrReset(user.getEmail(), 86400000, "activation");
        // mailService.sendActivationEmail(user.getEmail(), activationToken);

        log.info("User {} registered as PENDING and activation email sent.", user.getEmail());
        if (logTokens) {
            // log.info("[DEV] Activation token for {}: {}", user.getEmail(), activationToken);
            log.info("[DEV] Activate via backend: /api/auth/activate?token={}", activationToken);
        }
        return user;
    }

    public String login(LoginRequest loginRequest) throws BadCredentialsException, InvalidTokenException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(RuntimeException::new);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidTokenException();
        }
        String jwt = jwtService.generateToken(authentication);

        if (logTokens) {
            log.info("[DEV] Access JWT for {}: {}", user.getEmail(), jwt);
        }

        return jwtService.generateToken(authentication);
    }

    public void activateAccount(String token) {
        if (!jwtService.validateToken(token) || !"activation".equals(jwtService.getTypeFromJWT(token))) {
            throw new RuntimeException("Invalid activation token");
        }

        String email = jwtService.getUsernameFromJWT(token);
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        // mailService.sendWelcomeEmail(user.getEmail(), activationToken);
    }

    public void sendPasswordResetToken(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = jwtService.generateTokenToActivatonOrReset(email, 900000, "password_reset");
            mailService.sendResetEmail(email, resetToken);
            if (logTokens) {
                log.info("[DEV] Password reset token for {}: {}", email, resetToken);
            }
        });
    }

    public void resetPassword(String token, String newPassword) {
        if (!jwtService.validateToken(token) || !"password_reset".equals(jwtService.getTypeFromJWT(token))) {
            throw new RuntimeException("Invalid password reset token");
        }

        String email = jwtService.getUsernameFromJWT(token);
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

