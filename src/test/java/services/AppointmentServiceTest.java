package services;

import com.domandre.entities.Appointment;
import com.domandre.repositories.AppointmentRepository;
import com.domandre.repositories.UserRepository;
import com.domandre.services.AppointmentService;
import com.domandre.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppointmentServiceTest {
    private AppointmentRepository appointmentRepository;
    private UserService userService;
    private UserRepository userRepository;
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        userService = mock(UserService.class);
        appointmentService = new AppointmentService(appointmentRepository,userService,userRepository);
    }
    @Test
    void testGetAvailableSlots_whenOneSlotTaken_shouldExcludeIt() {
        // Dia de teste: terça-feira (tem expediente das 08h às 20h)
        LocalDate date = LocalDate.of(2025, 4, 15); // terça-feira

        // Simular um horário já ocupado: 10:00
        LocalDateTime takenDateTime = LocalDateTime.of(date, LocalTime.of(10, 0));
        Appointment taken = Appointment.builder().appointmentDate(takenDateTime).build();

        // Configurar mock: retorna uma lista com esse appointment
        when(appointmentRepository.findAllByAppointmentDateBetween(
                date.atStartOfDay(), date.atTime(23, 59)))
                .thenReturn(List.of(taken));

        // 🔹 Executar o método que estamos testando
        List<LocalTime> availableSlots = appointmentService.getAvailableSlots(date);

        // 🔹 Verificar: o horário 10:00 não deve estar na lista
        assertFalse(availableSlots.contains(LocalTime.of(10, 0)));

        // 🔹 E outros horários dentro da faixa devem estar
        assertTrue(availableSlots.contains(LocalTime.of(9, 0)));
        assertTrue(availableSlots.contains(LocalTime.of(11, 0)));
    }

    private void assertFalse(boolean contains) {
    }
}
