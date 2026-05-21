package services;

import com.domandre.controllers.AppointmentMessageController;
import com.domandre.controllers.response.AppointmentMessageDTO;
import com.domandre.entities.AppointmentMessage;
import com.domandre.entities.User;
import com.domandre.enums.Role;
import com.domandre.services.AppointmentMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AppointmentMessageControllerTest {

    @InjectMocks
    private AppointmentMessageController messageController;

    @Mock
    private AppointmentMessageService messageService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private AppointmentMessage buildMessage(String content) {
        User author = new User();
        author.setFirstName("Deise");
        author.setLastName("Admin");
        author.setRole(Role.ADMIN);

        return AppointmentMessage.builder()
                .id(1L)
                .author(author)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void addMessage_shouldReturnDtoWithAuthorAndContent() {
        Long appointmentId = 10L;
        String content = "Please bring your previous exams.";
        AppointmentMessage message = buildMessage(content);

        when(messageService.addMessage(appointmentId, content)).thenReturn(message);

        com.domandre.controllers.request.AppointmentMessageRequest request =
                new com.domandre.controllers.request.AppointmentMessageRequest();
        request.setContent(content);

        ResponseEntity<AppointmentMessageDTO> response = messageController.addMessage(appointmentId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AppointmentMessageDTO dto = response.getBody();
        assertNotNull(dto);
        assertEquals("Deise Admin", dto.getAuthorName());
        assertEquals(content, dto.getContent());
        assertEquals(Role.ADMIN.name(), dto.getRole());
        verify(messageService).addMessage(appointmentId, content);
    }
}
