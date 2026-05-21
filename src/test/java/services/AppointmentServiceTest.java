package services;

import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.exceptions.AppointmentNotCancellableException;
import com.domandre.repositories.AppointmentRepository;
import com.domandre.repositories.UserRepository;
import com.domandre.services.AppointmentService;
import com.domandre.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppointmentServiceTest {

    private AppointmentService appointmentService;
    private AppointmentRepository appointmentRepository;
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);

        appointmentService = new AppointmentService(
                appointmentRepository,
                userRepository,
                userService
        );
    }

    @Test
    void testGetAvailableSlots_whenOneSlotTaken_shouldExcludeIt() {
        LocalDate nextValidBusinessDay = getNextValidBusinessDay(DayOfWeek.WEDNESDAY);
        LocalTime takenTime = LocalTime.of(10, 0);
        LocalDateTime takenDateTime = LocalDateTime.of(nextValidBusinessDay, takenTime);
        Appointment takenAppointment = Appointment.builder().appointmentDate(takenDateTime).build();

        when(appointmentRepository.findAllByAppointmentDateBetween(
                nextValidBusinessDay.atStartOfDay(),
                nextValidBusinessDay.atTime(23, 59)
        )).thenReturn(List.of(takenAppointment));

        List<LocalTime> availableSlots = appointmentService.getAvailableSlots(nextValidBusinessDay);

        assertFalse(availableSlots.contains(takenTime), "10:00 should NOT be available");

        LocalTime before = takenTime.minusHours(1); // 09:00
        LocalTime after = takenTime.plusHours(1);   // 11:00

        assertTrue(availableSlots.contains(before), before + " should be available");
        assertTrue(availableSlots.contains(after), after + " should be available");
    }

    @Test
    void cancelAppointment_whenWithin24Hours_shouldThrowException() {
        UUID patientId = UUID.randomUUID();
        User patient = new User();
        patient.setId(patientId);

        Appointment appointment = Appointment.builder()
                .id(1L)
                .patient(patient)
                .status(AppointmentStatus.REQUESTED)
                .appointmentDate(LocalDateTime.now().plusHours(12))
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userService.getCurrentUser()).thenReturn(patient);

        assertThrows(AppointmentNotCancellableException.class, () -> appointmentService.cancelAppointment(1L));
    }

    @Test
    void cancelAppointment_whenMoreThan24HoursAway_shouldCancel() {
        UUID patientId = UUID.randomUUID();
        User patient = new User();
        patient.setId(patientId);

        Appointment appointment = Appointment.builder()
                .id(1L)
                .patient(patient)
                .status(AppointmentStatus.REQUESTED)
                .appointmentDate(LocalDateTime.now().plusHours(48))
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userService.getCurrentUser()).thenReturn(patient);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Appointment result = appointmentService.cancelAppointment(1L);

        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
    }

    private LocalDate getNextValidBusinessDay(DayOfWeek dayOfWeek) {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return date;
    }
}
