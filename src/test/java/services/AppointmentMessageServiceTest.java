package services;

import com.domandre.entities.Appointment;
import com.domandre.entities.AppointmentMessage;
import com.domandre.entities.User;
import com.domandre.enums.Role;
import com.domandre.exceptions.InsufficientPermissionsException;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.repositories.AppointmentMessageRepository;
import com.domandre.repositories.AppointmentRepository;
import com.domandre.services.AppointmentMessageService;
import com.domandre.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AppointmentMessageServiceTest {

    @InjectMocks
    private AppointmentMessageService messageService;

    @Mock
    private AppointmentMessageRepository messageRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private User buildUser(Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(role);
        return user;
    }

    private Appointment buildAppointmentOwnedBy(User patient) {
        Appointment appointment = Appointment.builder()
                .id(1L)
                .patient(patient)
                .build();
        return appointment;
    }

    @Test
    void addMessage_whenPatientOwnsAppointment_shouldSaveAndReturnMessage() {
        User patient = buildUser(Role.USER);
        Appointment appointment = buildAppointmentOwnedBy(patient);
        String content = "I have a question about my appointment.";

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userService.getCurrentUser()).thenReturn(patient);
        when(messageRepository.save(any(AppointmentMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentMessage result = messageService.addMessage(1L, content);

        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(patient, result.getAuthor());
        verify(messageRepository).save(any(AppointmentMessage.class));
    }

    @Test
    void addMessage_whenAuthorIsAdmin_shouldSaveRegardlessOfOwnership() {
        User admin = buildUser(Role.ADMIN);
        User differentPatient = buildUser(Role.USER);
        Appointment appointment = buildAppointmentOwnedBy(differentPatient);
        String content = "Your appointment is confirmed.";

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userService.getCurrentUser()).thenReturn(admin);
        when(messageRepository.save(any(AppointmentMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentMessage result = messageService.addMessage(1L, content);

        assertNotNull(result);
        assertEquals(content, result.getContent());
        verify(messageRepository).save(any(AppointmentMessage.class));
    }

    @Test
    void addMessage_whenAppointmentNotFound_shouldThrowException() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> messageService.addMessage(99L, "Hello"));
        verifyNoInteractions(messageRepository);
    }

    @Test
    void addMessage_whenPatientDoesNotOwnAppointment_shouldThrowException() {
        User owner = buildUser(Role.USER);
        User intruder = buildUser(Role.USER);
        Appointment appointment = buildAppointmentOwnedBy(owner);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userService.getCurrentUser()).thenReturn(intruder);

        assertThrows(InsufficientPermissionsException.class, () -> messageService.addMessage(1L, "Sneaky message"));
        verifyNoInteractions(messageRepository);
    }
}
