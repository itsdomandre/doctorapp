package services;

import com.domandre.controllers.AppointmentController;
import com.domandre.controllers.request.AppointmentRequest;
import com.domandre.controllers.request.AppointmentSearchRequest;
import com.domandre.controllers.request.AppointmentUpdateStatusRequest;
import com.domandre.controllers.response.AppointmentDTO;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.enums.Procedures;
import com.domandre.exceptions.AppointmentNotCancellableException;
import com.domandre.exceptions.InsufficientPermissionsException;
import com.domandre.services.AppointmentService;
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
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AppointmentControllerTest {

    @InjectMocks
    private AppointmentController appointmentController;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Appointment buildAppointment(AppointmentStatus status) {
        User patient = new User();
        patient.setFirstName("Andre");
        patient.setLastName("Patient");

        return Appointment.builder()
                .id(1L)
                .patient(patient)
                .procedure(Procedures.TOXINA_BOTULINICA)
                .status(status)
                .build();
    }

    @Test
    void createAppointment_whenValid_shouldReturnDto() {
        AppointmentRequest request = new AppointmentRequest();
        Appointment appointment = buildAppointment(AppointmentStatus.REQUESTED);
        when(appointmentService.createAppointment(request)).thenReturn(appointment);

        ResponseEntity<AppointmentDTO> response = appointmentController.createAppointment(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AppointmentDTO dto = response.getBody();
        assertNotNull(dto);
        assertEquals(AppointmentStatus.REQUESTED, dto.getStatus());
        assertEquals(Procedures.TOXINA_BOTULINICA, dto.getProcedure());
        assertEquals("Andre Patient", dto.getPatientName());
        verify(appointmentService).createAppointment(request);
    }

    @Test
    void getMyAppointments_shouldReturnPageOfDtos() {
        User currentUser = new User();
        currentUser.setFirstName("Andre");
        currentUser.setLastName("Patient");

        Appointment appointment = buildAppointment(AppointmentStatus.REQUESTED);
        Page<Appointment> page = new PageImpl<>(List.of(appointment));

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentService.getAppointmentsForCurrentUser(currentUser, null, 0, 10)).thenReturn(page);

        ResponseEntity<Page<AppointmentDTO>> response = appointmentController.getMyAppointments(0, 10, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(userService).getCurrentUser();
        verify(appointmentService).getAppointmentsForCurrentUser(currentUser, null, 0, 10);
    }

    @Test
    void getMyAppointments_withStatusFilter_shouldPassStatusToService() {
        User currentUser = new User();
        Appointment appointment = buildAppointment(AppointmentStatus.APPROVED);
        Page<Appointment> page = new PageImpl<>(List.of(appointment));

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentService.getAppointmentsForCurrentUser(currentUser, AppointmentStatus.APPROVED, 0, 10)).thenReturn(page);

        ResponseEntity<Page<AppointmentDTO>> response = appointmentController.getMyAppointments(0, 10, AppointmentStatus.APPROVED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentService).getAppointmentsForCurrentUser(currentUser, AppointmentStatus.APPROVED, 0, 10);
    }

    @Test
    void getAllAppointments_shouldReturnAllAsPage() {
        Appointment appointment = buildAppointment(AppointmentStatus.APPROVED);
        Page<Appointment> page = new PageImpl<>(List.of(appointment));
        when(appointmentService.getAllAppointments(0, 20)).thenReturn(page);

        ResponseEntity<Page<AppointmentDTO>> response = appointmentController.getAllAppointments(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(appointmentService).getAllAppointments(0, 20);
    }

    @Test
    void updateAppointment_whenApproved_shouldReturnUpdatedDtoWithDoctor() {
        UUID doctorId = UUID.randomUUID();
        AppointmentUpdateStatusRequest request = new AppointmentUpdateStatusRequest();
        request.setStatus(AppointmentStatus.APPROVED);
        request.setDoctorId(doctorId);

        User doctor = new User();
        doctor.setFirstName("Deise");
        doctor.setLastName("Doctor");

        Appointment updated = buildAppointment(AppointmentStatus.APPROVED);
        updated.setDoctor(doctor);

        when(appointmentService.updateAppointmentStatus(1L, AppointmentStatus.APPROVED, doctorId)).thenReturn(updated);

        ResponseEntity<AppointmentDTO> response = appointmentController.updateAppointment(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AppointmentDTO dto = response.getBody();
        assertNotNull(dto);
        assertEquals(AppointmentStatus.APPROVED, dto.getStatus());
        assertEquals("Deise Doctor", dto.getDoctorName());
        verify(appointmentService).updateAppointmentStatus(1L, AppointmentStatus.APPROVED, doctorId);
    }

    @Test
    void updateAppointment_whenRejected_shouldReturnRejectedStatus() {
        AppointmentUpdateStatusRequest request = new AppointmentUpdateStatusRequest();
        request.setStatus(AppointmentStatus.REJECTED);

        Appointment rejected = buildAppointment(AppointmentStatus.REJECTED);
        when(appointmentService.updateAppointmentStatus(1L, AppointmentStatus.REJECTED, null)).thenReturn(rejected);

        ResponseEntity<AppointmentDTO> response = appointmentController.updateAppointment(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(AppointmentStatus.REJECTED, response.getBody().getStatus());
    }

    @Test
    void getAvailableSlots_whenDayHasSlots_shouldReturnFormattedStrings() {
        LocalDate date = LocalDate.now().plusDays(1);
        List<LocalTime> slots = List.of(LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(appointmentService.getAvailableSlots(date)).thenReturn(slots);

        ResponseEntity<List<String>> response = appointmentController.getAvailableSlots(date);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of("09:00", "10:00", "11:00"), response.getBody());
        verify(appointmentService).getAvailableSlots(date);
    }

    @Test
    void getAvailableSlots_whenClosedDay_shouldReturnEmptyList() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(appointmentService.getAvailableSlots(date)).thenReturn(List.of());

        ResponseEntity<List<String>> response = appointmentController.getAvailableSlots(date);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void cancelAppointment_whenOwner_shouldReturnCancelledDto() {
        Appointment cancelled = buildAppointment(AppointmentStatus.CANCELLED);
        when(appointmentService.cancelAppointment(1L)).thenReturn(cancelled);

        ResponseEntity<AppointmentDTO> response = appointmentController.cancelAppointment(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(AppointmentStatus.CANCELLED, response.getBody().getStatus());
        verify(appointmentService).cancelAppointment(1L);
    }

    @Test
    void cancelAppointment_whenStatusIsCompleted_shouldThrowException() {
        when(appointmentService.cancelAppointment(1L)).thenThrow(new AppointmentNotCancellableException());

        assertThrows(AppointmentNotCancellableException.class, () -> appointmentController.cancelAppointment(1L));
    }

    @Test
    void cancelAppointment_whenWithin24Hours_shouldThrowException() {
        when(appointmentService.cancelAppointment(1L)).thenThrow(new AppointmentNotCancellableException());

        assertThrows(AppointmentNotCancellableException.class, () -> appointmentController.cancelAppointment(1L));
    }

    @Test
    void cancelAppointment_whenNotOwner_shouldThrowInsufficientPermissions() {
        when(appointmentService.cancelAppointment(1L)).thenThrow(new InsufficientPermissionsException());

        assertThrows(InsufficientPermissionsException.class, () -> appointmentController.cancelAppointment(1L));
    }

    @Test
    void getPendingAppointments_shouldReturnListOfRequestedDtos() {
        List<Appointment> appointments = List.of(buildAppointment(AppointmentStatus.REQUESTED));
        when(appointmentService.getPendingAppointments()).thenReturn(appointments);

        ResponseEntity<List<AppointmentDTO>> response = appointmentController.getPendingAppointments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(AppointmentStatus.REQUESTED, response.getBody().get(0).getStatus());
        verify(appointmentService).getPendingAppointments();
    }

    @Test
    void getTodayAppointments_shouldReturnPageOfDtos() {
        Appointment appointment = buildAppointment(AppointmentStatus.APPROVED);
        Page<Appointment> page = new PageImpl<>(List.of(appointment));
        when(appointmentService.getTodayAppointments(0, 20)).thenReturn(page);

        ResponseEntity<Page<AppointmentDTO>> response = appointmentController.getTodayAppointments(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(appointmentService).getTodayAppointments(0, 20);
    }

    @Test
    void getSearchAppointments_shouldReturnMatchingDtos() {
        AppointmentSearchRequest searchRequest = new AppointmentSearchRequest();
        searchRequest.setPatientName("Andre");
        List<Appointment> results = List.of(buildAppointment(AppointmentStatus.APPROVED));
        when(appointmentService.searchAppointments(searchRequest)).thenReturn(results);

        ResponseEntity<List<AppointmentDTO>> response = appointmentController.getSearchAppointments(searchRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(AppointmentStatus.APPROVED, response.getBody().get(0).getStatus());
        verify(appointmentService).searchAppointments(searchRequest);
    }
}
