package services;

import com.domandre.controllers.AnamnesisController;
import com.domandre.controllers.request.AnamnesisRequest;
import com.domandre.controllers.response.AnamnesisDTO;
import com.domandre.entities.Anamnesis;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.exceptions.AppointmentNotAprovedException;
import com.domandre.services.AnamnesisService;
import com.domandre.services.AppointmentService;
import com.domandre.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
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

    @Mock
    private UserService userService;

    private UUID patientId;
    private long appointmentId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        patientId = UUID.randomUUID();
        appointmentId = 2;
    }

    private Anamnesis buildAnamnesis() {
        Anamnesis a = new Anamnesis();
        a.setConsumesAlcohol(true);
        a.setHadCancer(false);
        a.setSmokes(false);
        return a;
    }

    private Appointment approvedAppointment() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.APPROVED);
        return appointment;
    }

    // ── getAnamnesisTemplate (existing coverage, kept + extended) ─────────────

    @Test
    void getAnamnesisTemplate_whenApprovedAndLastExists_shouldReturnDto() {
        when(appointmentService.getOrThrow(appointmentId)).thenReturn(approvedAppointment());
        Anamnesis last = buildAnamnesis();
        when(anamnesisService.getLastByPatient(patientId)).thenReturn(last);

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
    void getAnamnesisTemplate_whenNotApproved_shouldThrowException() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.REQUESTED);
        when(appointmentService.getOrThrow(appointmentId)).thenReturn(appointment);

        assertThrows(AppointmentNotAprovedException.class,
                () -> anamnesisController.getAnamnesisTemplate(patientId, appointmentId));

        verify(appointmentService).getOrThrow(appointmentId);
        verifyNoInteractions(anamnesisService);
    }

    @Test
    void getAnamnesisTemplate_whenApprovedButNoLastAnamnesis_shouldReturnEmptyDto() {
        when(appointmentService.getOrThrow(appointmentId)).thenReturn(approvedAppointment());
        when(anamnesisService.getLastByPatient(patientId)).thenReturn(null);

        ResponseEntity<AnamnesisDTO> response = anamnesisController.getAnamnesisTemplate(patientId, appointmentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ── Admin: getByPatient ───────────────────────────────────────────────────

    @Test
    void getByPatient_shouldReturnListOfDtos() {
        List<Anamnesis> anamneses = List.of(buildAnamnesis(), buildAnamnesis());
        when(anamnesisService.getAnamnesesByPatient(patientId)).thenReturn(anamneses);

        ResponseEntity<List<AnamnesisDTO>> response = anamnesisController.getByPatient(patientId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(anamnesisService).getAnamnesesByPatient(patientId);
    }

    @Test
    void getByPatient_whenNoAnamneses_shouldReturnEmptyList() {
        when(anamnesisService.getAnamnesesByPatient(patientId)).thenReturn(List.of());

        ResponseEntity<List<AnamnesisDTO>> response = anamnesisController.getByPatient(patientId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ── Admin: createAnamnesis ────────────────────────────────────────────────

    @Test
    void createAnamnesis_shouldReturnCreatedDto() {
        AnamnesisRequest request = new AnamnesisRequest();
        request.setConsumesAlcohol(false);
        request.setSmokes(true);

        Anamnesis created = buildAnamnesis();
        when(anamnesisService.createAnamnesis(patientId, appointmentId, request)).thenReturn(created);

        ResponseEntity<AnamnesisDTO> response = anamnesisController.createAnamnesis(patientId, appointmentId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(anamnesisService).createAnamnesis(patientId, appointmentId, request);
    }

    // ── Admin: updateAnamnesisByAppointment ───────────────────────────────────

    @Test
    void updateAnamnesisByAppointment_shouldReturnUpdatedDto() {
        AnamnesisRequest request = new AnamnesisRequest();
        request.setHasDiabetes(true);

        Anamnesis updated = buildAnamnesis();
        updated.setHasDiabetes(true);
        when(anamnesisService.updateByAppointmentId(appointmentId, request)).thenReturn(updated);

        ResponseEntity<AnamnesisDTO> response = anamnesisController.updateAnamnesisByAppointment(appointmentId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getHasDiabetes());
        verify(anamnesisService).updateByAppointmentId(appointmentId, request);
    }

    // ── Admin: getLastByPatient ───────────────────────────────────────────────

    @Test
    void getLastByPatient_whenExists_shouldReturnDto() {
        Anamnesis last = buildAnamnesis();
        when(anamnesisService.getLastByPatient(patientId)).thenReturn(last);

        ResponseEntity<AnamnesisDTO> response = anamnesisController.getLastByPatient(patientId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(anamnesisService).getLastByPatient(patientId);
    }

    @Test
    void getLastByPatient_whenNone_shouldReturn404() {
        when(anamnesisService.getLastByPatient(patientId)).thenReturn(null);

        ResponseEntity<AnamnesisDTO> response = anamnesisController.getLastByPatient(patientId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── Admin: getAnamnesisHistory ────────────────────────────────────────────

    @Test
    void getAnamnesisHistory_shouldReturnFilteredDtos() {
        List<Anamnesis> history = List.of(buildAnamnesis());
        when(anamnesisService.getAnamnesisHistory(patientId, null, null)).thenReturn(history);

        ResponseEntity<List<AnamnesisDTO>> response = anamnesisController.getAnamnesisHistory(patientId, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(anamnesisService).getAnamnesisHistory(patientId, null, null);
    }

    // ── Patient: createMyAnamnesis ────────────────────────────────────────────

    @Test
    void createMyAnamnesis_shouldUseCurrentUserAndReturnDto() {
        User patient = new User();
        patient.setId(patientId);

        AnamnesisRequest request = new AnamnesisRequest();
        request.setConsumesAlcohol(true);

        Anamnesis created = buildAnamnesis();
        when(userService.getCurrentUser()).thenReturn(patient);
        when(anamnesisService.createForPatient(patient, appointmentId, request)).thenReturn(created);

        ResponseEntity<AnamnesisDTO> response = anamnesisController.createMyAnamnesis(appointmentId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService).getCurrentUser();
        verify(anamnesisService).createForPatient(patient, appointmentId, request);
    }

    // ── Patient: getMyAnamnesis ───────────────────────────────────────────────

    @Test
    void getMyAnamnesis_shouldReturnCurrentPatientAnamnesis() {
        User patient = new User();
        Anamnesis anamnesis = buildAnamnesis();
        when(userService.getCurrentUser()).thenReturn(patient);
        when(anamnesisService.getByAppointmentIdForPatient(appointmentId, patient)).thenReturn(anamnesis);

        ResponseEntity<AnamnesisDTO> response = anamnesisController.getMyAnamnesis(appointmentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getHadCancer());
        verify(userService).getCurrentUser();
        verify(anamnesisService).getByAppointmentIdForPatient(appointmentId, patient);
    }

    // ── Patient: getMyTemplate ────────────────────────────────────────────────

    @Test
    void getMyTemplate_whenLastExists_shouldReturnDto() {
        User patient = new User();
        patient.setId(patientId);
        Anamnesis last = buildAnamnesis();
        when(userService.getCurrentUser()).thenReturn(patient);
        when(anamnesisService.getLastByPatient(patientId)).thenReturn(last);

        ResponseEntity<AnamnesisDTO> response = anamnesisController.getMyTemplate();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getConsumesAlcohol());
        verify(anamnesisService).getLastByPatient(patientId);
    }

    @Test
    void getMyTemplate_whenNoPreviousAnamnesis_shouldReturnEmptyDto() {
        User patient = new User();
        patient.setId(patientId);
        when(userService.getCurrentUser()).thenReturn(patient);
        when(anamnesisService.getLastByPatient(patientId)).thenReturn(null);

        ResponseEntity<AnamnesisDTO> response = anamnesisController.getMyTemplate();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
