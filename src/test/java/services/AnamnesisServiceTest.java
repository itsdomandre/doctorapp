package services;

import com.domandre.controllers.request.AnamnesisRequest;
import com.domandre.entities.Anamnesis;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.exceptions.AnamnesisAlreadyExistsException;
import com.domandre.exceptions.AppointmentNotAprovedException;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.repositories.AnamnesisRepository;
import com.domandre.repositories.AppointmentRepository;
import com.domandre.services.AnamnesisService;
import com.domandre.services.AppointmentService;
import com.domandre.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnamnesisServiceTest {

    @InjectMocks
    private AnamnesisService anamnesisService;

    @Mock
    private AnamnesisRepository anamnesisRepository;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private UserService userService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User buildPatient() {
        User patient = new User();
        patient.setId(UUID.randomUUID());
        patient.setFirstName("Andre");
        patient.setLastName("Patient");
        return patient;
    }

    private Appointment buildApprovedAppointment() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.APPROVED);
        appointment.setAnamnesis(null);
        return appointment;
    }

    // ── updateByAppointmentId (existing coverage) ─────────────────────────────

    @Test
    void updateByAppointmentId_shouldUpdateFieldsAndSave() {
        AnamnesisRequest request = new AnamnesisRequest();
        request.setHasChronicDisease(true);
        request.setChronicDiseaseDescription("Hypertension");

        Anamnesis anamnesis = new Anamnesis();
        Appointment appointment = new Appointment();
        appointment.setAnamnesis(anamnesis);

        when(appointmentService.getOrThrow(1L)).thenReturn(appointment);
        when(anamnesisRepository.save(any(Anamnesis.class))).thenAnswer(inv -> inv.getArgument(0));

        Anamnesis updated = anamnesisService.updateByAppointmentId(1L, request);

        assertNotNull(updated);
        assertTrue(updated.getHasChronicDisease());
        assertEquals("Hypertension", updated.getChronicDiseaseDescription());
        verify(anamnesisRepository).save(anamnesis);
    }

    @Test
    void updateByAppointmentId_whenAnamnesisIsNull_shouldThrowException() {
        Appointment appointment = new Appointment();
        appointment.setAnamnesis(null);

        when(appointmentService.getOrThrow(1L)).thenReturn(appointment);

        assertThrows(ResourceNotFoundException.class,
                () -> anamnesisService.updateByAppointmentId(1L, new AnamnesisRequest()));
        verify(anamnesisRepository, never()).save(any());
    }

    // ── createAnamnesis ───────────────────────────────────────────────────────

    @Test
    void createAnamnesis_whenApprovedAndNoExisting_shouldCreate() {
        UUID patientId = UUID.randomUUID();
        User patient = buildPatient();
        patient.setId(patientId);
        Appointment appointment = buildApprovedAppointment();

        AnamnesisRequest request = new AnamnesisRequest();
        request.setConsumesAlcohol(true);

        when(userService.getUserById(patientId)).thenReturn(patient);
        when(appointmentService.getOrThrow(1L)).thenReturn(appointment);
        when(anamnesisRepository.save(any(Anamnesis.class))).thenAnswer(inv -> inv.getArgument(0));

        Anamnesis result = anamnesisService.createAnamnesis(patientId, 1L, request);

        assertNotNull(result);
        assertEquals(patient, result.getPatient());
        verify(anamnesisRepository).save(any(Anamnesis.class));
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void createAnamnesis_whenAppointmentNotApproved_shouldThrowException() {
        UUID patientId = UUID.randomUUID();
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.REQUESTED);

        when(userService.getUserById(patientId)).thenReturn(buildPatient());
        when(appointmentService.getOrThrow(1L)).thenReturn(appointment);

        assertThrows(AppointmentNotAprovedException.class,
                () -> anamnesisService.createAnamnesis(patientId, 1L, new AnamnesisRequest()));
        verifyNoInteractions(anamnesisRepository);
    }

    @Test
    void createAnamnesis_whenAnamnesisAlreadyExists_shouldThrowException() {
        UUID patientId = UUID.randomUUID();
        Appointment appointment = buildApprovedAppointment();
        appointment.setAnamnesis(new Anamnesis());

        when(userService.getUserById(patientId)).thenReturn(buildPatient());
        when(appointmentService.getOrThrow(1L)).thenReturn(appointment);

        assertThrows(AnamnesisAlreadyExistsException.class,
                () -> anamnesisService.createAnamnesis(patientId, 1L, new AnamnesisRequest()));
        verifyNoInteractions(anamnesisRepository);
    }

    // ── getLastByPatient ──────────────────────────────────────────────────────

    @Test
    void getLastByPatient_shouldReturnMostRecent() {
        UUID patientId = UUID.randomUUID();
        Anamnesis latest = new Anamnesis();
        latest.setSmokes(true);

        when(anamnesisRepository.findTopByPatientIdOrderByCreatedAtDesc(patientId)).thenReturn(latest);

        Anamnesis result = anamnesisService.getLastByPatient(patientId);

        assertNotNull(result);
        assertTrue(result.getSmokes());
    }

    @Test
    void getLastByPatient_whenNone_shouldReturnNull() {
        UUID patientId = UUID.randomUUID();
        when(anamnesisRepository.findTopByPatientIdOrderByCreatedAtDesc(patientId)).thenReturn(null);

        assertNull(anamnesisService.getLastByPatient(patientId));
    }

    // ── getAnamnesesByPatient ─────────────────────────────────────────────────

    @Test
    void getAnamnesesByPatient_shouldReturnAll() {
        UUID patientId = UUID.randomUUID();
        List<Anamnesis> list = List.of(new Anamnesis(), new Anamnesis());
        when(anamnesisRepository.findAllByPatientId(patientId)).thenReturn(list);

        List<Anamnesis> result = anamnesisService.getAnamnesesByPatient(patientId);

        assertEquals(2, result.size());
    }
}
