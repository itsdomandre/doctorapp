package com.domandre.services;

import com.domandre.controllers.request.LoginRequest;
import com.domandre.controllers.request.RegisterRequest;
import com.domandre.entities.InvalidToken;
import com.domandre.entities.User;
import com.domandre.enums.UserStatus;
import com.domandre.exceptions.AccountNotVerifiedException;
import com.domandre.exceptions.EmailIntegrationErrorException;
import com.domandre.exceptions.InvalidTokenException;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.exceptions.UserAlreadyExistsException;
import com.domandre.repositories.InvalidTokenRepository;
import com.domandre.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
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
    private final MailService mailService;
    private final InvalidTokenRepository invalidTokenRepository;

    @Value("${app.log.tokens:false}")
    private boolean logTokens;

    @Value("${auth.activation.expiration-ms:86400000}")
    private long activationTokenTtlMs;

    public User register(RegisterRequest request) {
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
        user.setRole(Role.USER);
        user.setStatus(UserStatus.UNVERIFIED);

        userRepository.save(user);
        String activationToken = jwtService.generateTokenToActivatonOrReset(user.getEmail(), activationTokenTtlMs, "activation");
        mailService.sendActivationEmail(user.getEmail(), activationToken);
        log.info("User {} registered as PENDING and activation email sent.", user.getEmail());
        if (logTokens) {
            log.info("[DEV] Activate via backend: /api/auth/activate?token={}", activationToken);
        }
        return user;
    }

    public String login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(ResourceNotFoundException::new);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotVerifiedException();
        }
        String jwt = jwtService.generateToken(authentication);
        if (logTokens) {
            log.info("[DEV] Access JWT for {}: {}", user.getEmail(), jwt);
        }
        return jwt;
    }

    public void activateAccount(String token) {
        if (!jwtService.validateToken(token) || !"activation".equals(jwtService.getTypeFromJWT(token))) {
            throw new InvalidTokenException();
        }

        String email = jwtService.getUsernameFromJWT(token);
        User user = userRepository.findByEmail(email).orElseThrow(ResourceNotFoundException::new);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        mailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        log.info("Account activated: {}", email);
    }

    public void sendPasswordResetToken(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            String newToken = jwtService.generateTokenToActivatonOrReset(email, 900000, "password_reset");
            mailService.sendResetEmailPassword(email, newToken);
        }
    }

    public void resetPassword(String token, String newPassword) {
        if (!jwtService.validateToken(token) || !"password_reset".equals(jwtService.getTypeFromJWT(token))) {
            throw new InvalidTokenException();
        }
        String email = jwtService.getUsernameFromJWT(token);
        User user = userRepository.findByEmail(email).orElseThrow(ResourceNotFoundException::new);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void logout(String jwt) {
        invalidTokenRepository.save(new InvalidToken(jwt, jwtService.getExpirationFromJWT(jwt)));
    }

    public void resendActivation(String email) {
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (user.getStatus() == UserStatus.UNVERIFIED) {
            String token = jwtService.generateTokenToActivatonOrReset(email, activationTokenTtlMs, "activation");
            mailService.sendActivationEmail(email, token);
            if (logTokens) {
                log.info("[DEV] Activate via backend: /api/auth/activate?token={}", token);
            }
        } else {
            log.info("Resend activation called for {} but status is {}", email, user.getStatus());
        }
    }
}

