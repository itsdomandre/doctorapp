package services;

import com.domandre.controllers.AnamnesisController;
import com.domandre.controllers.response.AnamnesisDTO;
import com.domandre.entities.Anamnesis;
import com.domandre.entities.Appointment;
import com.domandre.enums.AppointmentStatus;
import com.domandre.exceptions.AppointmentNotAprovedException;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.services.AnamnesisService;
import com.domandre.services.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AnamnesisControllerTest {
    @InjectMocks
    private AnamnesisController anamnesisController;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AnamnesisService anamnesisService;

    private UUID patientId;
    private long appointmentId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        patientId = UUID.randomUUID();
        appointmentId = 2;
    }

    @Test
    void getTemplateLastAnamnesis_whenLastExists_shouldReturnDto() throws ResourceNotFoundException, AppointmentNotAprovedException {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.APPROVED);
        when(appointmentService.getOrThrow(appointmentId)).thenReturn(appointment);

        Anamnesis lastAnamnesis = new Anamnesis();
        lastAnamnesis.setConsumesAlcohol(true);
        lastAnamnesis.setHadCancer(false);
        when(anamnesisService.getLastByPatient(patientId)).thenReturn(lastAnamnesis);

        ResponseEntity<AnamnesisDTO> response = anamnesisController.getAnamnesisTemplate(patientId, appointmentId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AnamnesisDTO dto = response.getBody();
        assertNotNull(dto);
        assertTrue(dto.getConsumesAlcohol());
        assertFalse(dto.getHadCancer());
        verify(appointmentService).getOrThrow(appointmentId);
        verify(anamnesisService).getLastByPatient(patientId);
    }

    @Test
    void getAnamnesisTemplate_whenNotApproved_shouldThrowException() throws ResourceNotFoundException {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.REQUESTED);
        when(appointmentService.getOrThrow(appointmentId)).thenReturn(appointment);

        assertThrows(AppointmentNotAprovedException.class, () -> anamnesisController.getAnamnesisTemplate(patientId, appointmentId));

        verify(appointmentService).getOrThrow(appointmentId);
        verifyNoInteractions(anamnesisService);
    }
}
