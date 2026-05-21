package services;

import com.domandre.controllers.request.AdminCreateUserRequest;
import com.domandre.controllers.response.UserDTO;
import com.domandre.entities.User;
import com.domandre.enums.Role;
import com.domandre.enums.UserStatus;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.exceptions.UserAlreadyExistsException;
import com.domandre.repositories.UserRepository;
import com.domandre.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private User buildUser(Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Andre");
        user.setLastName("Patient");
        user.setEmail("andre@example.com");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setBirthdate(LocalDate.of(1995, 5, 20));
        return user;
    }

    // ── adminCreateUser ───────────────────────────────────────────────────────

    @Test
    void adminCreateUser_whenEmailNotExists_shouldReturnDto() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setFirstName("Andre");
        request.setLastName("Patient");
        request.setEmail("novo@example.com");
        request.setPassword("P4$$w0rd");
        request.setPhoneNumber("11999999999");
        request.setBirthdate(LocalDate.of(1995, 5, 20));
        request.setRole(Role.USER);

        User saved = buildUser(Role.USER);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDTO result = userService.adminCreateUser(request);

        assertNotNull(result);
        assertEquals("Andre Patient", result.getFullName());
        assertEquals(Role.USER, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void adminCreateUser_whenEmailAlreadyExists_shouldThrowException() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.adminCreateUser(request));
        verify(userRepository, never()).save(any());
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_whenExists_shouldReturnUser() {
        UUID id = UUID.randomUUID();
        User user = buildUser(Role.USER);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_whenNotFound_shouldThrowException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(id));
    }

    // ── updateUserRole ────────────────────────────────────────────────────────

    @Test
    void updateUserRole_shouldUpdateAndReturnDto() {
        UUID id = UUID.randomUUID();
        User user = buildUser(Role.USER);
        User updated = buildUser(Role.ADMIN);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(updated);

        UserDTO result = userService.updateUserRole(id, Role.ADMIN);

        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository).save(user);
    }

    // ── getAllPatients ────────────────────────────────────────────────────────

    @Test
    void getAllPatients_shouldReturnPageOfUserRoleDtos() {
        User patient = buildUser(Role.USER);
        Page<User> userPage = new PageImpl<>(List.of(patient));
        when(userRepository.findByRole(eq(Role.USER), any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.getAllPatients(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(Role.USER, result.getContent().get(0).getRole());
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_shouldReturnPageOfAllDtos() {
        Page<User> userPage = new PageImpl<>(List.of(buildUser(Role.USER), buildUser(Role.ADMIN)));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.getAllUsers(0, 10);

        assertEquals(2, result.getTotalElements());
    }
}
