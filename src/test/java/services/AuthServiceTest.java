package services;

import com.domandre.controllers.request.LoginRequest;
import com.domandre.controllers.request.RegisterRequest;
import com.domandre.entities.User;
import com.domandre.enums.Role;
import com.domandre.exceptions.UserAlreadyExistsException;
import com.domandre.repositories.InvalidTokenRepository;
import com.domandre.repositories.UserRepository;
import com.domandre.services.AuthService;
import com.domandre.services.JwtService;
import com.domandre.services.MailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private InvalidTokenRepository invalidTokenRepository;
    @Mock private MailService mailService;

    @InjectMocks private AuthService authService;

    @Test
    void register_whenEmailNotExists_shouldRegisterNewUser() throws UserAlreadyExistsException {
        RegisterRequest request = new RegisterRequest("Ruud", "Van Nistelrooy", "ruud@example.com", "1234", "11991238863", LocalDate.of(2001,10, 8), Role.USER);
        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("HashedPassword");

        User savedUser = new User();
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        User result = authService.register(request);

        assertNotNull(result);
        then(userRepository).should().save(any(User.class));
    }
    @Test
    void register_whenEmailAlreadyExists_shouldThrowUserAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest("Jane", "Doe", "jane@example.com", "password", "999999999", null, null);

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void login_whenCredentialsAreCorrect_shouldReturnToken() {
        LoginRequest loginRequest = new LoginRequest("john@example.com", "password");
        Authentication authentication = mock(Authentication.class);

        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(jwtService.generateToken(authentication)).willReturn("mockedToken");

        String token = authService.login(loginRequest);

        assertEquals("mockedToken", token);
    }

    @Test
    void isTokenBlacklisted_whenTokenIsBlacklisted_shouldReturnTrue() {
        given(invalidTokenRepository.existsByToken("blacklistedToken")).willReturn(true);

        boolean result = authService.isTokenBlacklisted("blacklistedToken");

        assertTrue(result);
    }
}
