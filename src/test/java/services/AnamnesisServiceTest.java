package com.domandre.services;

import com.domandre.controllers.request.AnamnesisRequest;
import com.domandre.entities.Anamnesis;
import com.domandre.entities.Appointment;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.repositories.AnamnesisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnamnesisServiceTest {

    @InjectMocks
    private AnamnesisService anamnesisService;
    @Mock
    private AnamnesisRepository anamnesisRepository;
    @Mock
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateByAppointmentId_shouldUpdateFieldsAndSave() throws ResourceNotFoundException {
        Long appointmentId = 1L;

        AnamnesisRequest request = new AnamnesisRequest();
        request.setHasChronicDisease(true);
        request.setChronicDiseaseDescription("Hypertension");

        Anamnesis anamnesis = new Anamnesis();
        Appointment appointment = new Appointment();
        appointment.setAnamnesis(anamnesis);

        when(appointmentService.getOrThrow(appointmentId)).thenReturn(appointment);
        when(anamnesisRepository.save(any(Anamnesis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Anamnesis updated = anamnesisService.updateByAppointmentId(appointmentId, request);

        assertNotNull(updated);
        assertTrue(updated.getHasChronicDisease());
        assertEquals("Hypertension", updated.getChronicDiseaseDescription());
        verify(anamnesisRepository, times(1)).save(anamnesis);
    }

    @Test
    void updateByAppointmentId_shouldThrowExceptionWhenAnamnesisIsNull() throws ResourceNotFoundException {
        Long appointmentId = 1L;
        AnamnesisRequest request = new AnamnesisRequest();
        Appointment appointment = new Appointment();
        appointment.setAnamnesis(null);

        when(appointmentService.getOrThrow(appointmentId)).thenReturn(appointment);

        assertThrows(ResourceNotFoundException.class, () -> {
            anamnesisService.updateByAppointmentId(appointmentId, request);
        });

        verify(anamnesisRepository, never()).save(any());
    }
}
