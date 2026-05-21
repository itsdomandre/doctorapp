package services;

import com.domandre.controllers.UserController;
import com.domandre.controllers.request.AdminCreateUserRequest;
import com.domandre.controllers.request.UpdateRoleRequest;
import com.domandre.controllers.response.UserDTO;
import com.domandre.enums.Role;
import com.domandre.enums.UserStatus;
import com.domandre.exceptions.UserAlreadyExistsException;
import com.domandre.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private UserDTO buildUserDTO(Role role) {
        UserDTO dto = new UserDTO();
        dto.setId(UUID.randomUUID());
        dto.setFullName("Andre Patient");
        dto.setEmail("andre@example.com");
        dto.setRole(role);
        dto.setStatus(UserStatus.ACTIVE);
        return dto;
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_whenValid_shouldReturn201WithDto() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("novo@example.com");
        request.setRole(Role.USER);

        UserDTO dto = buildUserDTO(Role.USER);
        when(userService.adminCreateUser(request)).thenReturn(dto);

        ResponseEntity<UserDTO> response = userController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Role.USER, response.getBody().getRole());
        verify(userService).adminCreateUser(request);
    }

    @Test
    void createUser_whenEmailAlreadyExists_shouldThrowException() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("existing@example.com");

        when(userService.adminCreateUser(request)).thenThrow(new UserAlreadyExistsException());

        assertThrows(UserAlreadyExistsException.class, () -> userController.createUser(request));
    }

    // ── updateUserRole ────────────────────────────────────────────────────────

    @Test
    void updateUserRole_shouldReturnDtoWithNewRole() {
        UUID userId = UUID.randomUUID();
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole(Role.ADMIN);

        UserDTO dto = buildUserDTO(Role.ADMIN);
        when(userService.updateUserRole(userId, Role.ADMIN)).thenReturn(dto);

        ResponseEntity<UserDTO> response = userController.updateUserRole(userId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Role.ADMIN, response.getBody().getRole());
        verify(userService).updateUserRole(userId, Role.ADMIN);
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_shouldReturnPageOfDtos() {
        Page<UserDTO> page = new PageImpl<>(List.of(buildUserDTO(Role.USER), buildUserDTO(Role.ADMIN)));
        when(userService.getAllUsers(0, 10)).thenReturn(page);

        ResponseEntity<Page<UserDTO>> response = userController.getAllUsers(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTotalElements());
        verify(userService).getAllUsers(0, 10);
    }

    // ── getAllPatients ────────────────────────────────────────────────────────

    @Test
    void getAllPatients_shouldReturnPageOfPatientDtos() {
        Page<UserDTO> page = new PageImpl<>(List.of(buildUserDTO(Role.USER)));
        when(userService.getAllPatients(0, 10)).thenReturn(page);

        ResponseEntity<Page<UserDTO>> response = userController.getAllPatients(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(Role.USER, response.getBody().getContent().get(0).getRole());
        verify(userService).getAllPatients(0, 10);
    }
}
