package services;

import com.domandre.entities.Appointment;
import com.domandre.helpers.BusinessHoursHelper;
import com.domandre.helpers.BusinessHoursHelper.TimeRange;
import com.domandre.repositories.AnamnesisRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppointmentServiceTest {

    private AppointmentService appointmentService;
    private AppointmentRepository appointmentRepository;
    private UserRepository userRepository;
    private UserService userService;
    private AnamnesisRepository anamnesisRepository;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        anamnesisRepository = mock(AnamnesisRepository.class);

        appointmentService = new AppointmentService(
                appointmentRepository,
                userService,
                userRepository,
                anamnesisRepository
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
        System.out.println("Available slots: " + availableSlots);

        assertFalse(availableSlots.contains(takenTime), "10:00 should NOT be available");

        LocalTime before = takenTime.minusHours(1); // 09:00
        LocalTime after = takenTime.plusHours(1);   // 11:00

        assertTrue(availableSlots.contains(before), before + " should be available");
        assertTrue(availableSlots.contains(after), after + " should be available");
    }

    private LocalDate getNextValidBusinessDay(DayOfWeek dayOfWeek) {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return date;
    }
}
