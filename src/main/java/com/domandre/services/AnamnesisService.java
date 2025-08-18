package com.domandre.services;

import com.domandre.controllers.request.AnamnesisRequest;
import com.domandre.entities.Anamnesis;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.exceptions.AnamnesisAlreadyExistsException;
import com.domandre.exceptions.AppointmentNotAprovedException;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.mappers.AnamnesisMapper;
import com.domandre.repositories.AnamnesisRepository;
import com.domandre.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnamnesisService {
    private final AnamnesisRepository repository;
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final AppointmentRepository appointmentRepository;

    public List<Anamnesis> getAnamnesesByPatient(UUID patientId) {
        return repository.findAllByPatientId(patientId);
    }

    public Anamnesis getLastByPatient(UUID patientId) {
        return repository.findTopByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public Anamnesis createAnamnesis(UUID patientId, Long appointmentId, AnamnesisRequest request) throws ResourceNotFoundException, AppointmentNotAprovedException, AnamnesisAlreadyExistsException {
        User patient = userService.getUserById(patientId);
        LocalDateTime now = LocalDateTime.now();

        Appointment appointment = appointmentService.getOrThrow(appointmentId);
        if (!AppointmentStatus.APPROVED.equals(appointment.getStatus())) {
            log.info("Anamnesis can only created for approved appointment");
            throw new AppointmentNotAprovedException();
        }

        if (appointment.getAnamnesis() != null) {
            throw new AnamnesisAlreadyExistsException();
        }

        Anamnesis anamnesis = AnamnesisMapper.fromRequest(request);
        anamnesis.setPatient(patient);
        anamnesis.setAppointment(appointment);
        anamnesis.setCreatedAt(LocalDateTime.now());

        Anamnesis createdAnamnesis = repository.save(anamnesis);
        appointment.setAnamnesis(createdAnamnesis);
        appointmentRepository.save(appointment);

        return createdAnamnesis;
    }

    public Anamnesis updateByAppointmentId(Long appointmentId, AnamnesisRequest request) throws ResourceNotFoundException {
        Appointment appointment = appointmentService.getOrThrow(appointmentId);
        Anamnesis anamnesis = appointment.getAnamnesis();

        if (anamnesis == null) {
            throw new ResourceNotFoundException();
        }

        if (request.getHasChronicDisease() != null) anamnesis.setHasChronicDisease(request.getHasChronicDisease());
        if (request.getChronicDiseaseDescription() != null)
            anamnesis.setChronicDiseaseDescription(request.getChronicDiseaseDescription());
        if (request.getUsesContinuousMedication() != null)
            anamnesis.setUsesContinuousMedication(request.getUsesContinuousMedication());
        if (request.getMedicationDescription() != null)
            anamnesis.setMedicationDescription(request.getMedicationDescription());
        if (request.getHasAllergies() != null) anamnesis.setHasAllergies(request.getHasAllergies());
        if (request.getAllergyDescription() != null) anamnesis.setAllergyDescription(request.getAllergyDescription());
        if (request.getHadAestheticTreatment() != null)
            anamnesis.setHadAestheticTreatment(request.getHadAestheticTreatment());
        if (request.getTreatmentDescription() != null)
            anamnesis.setTreatmentDescription(request.getTreatmentDescription());
        if (request.getHasTattoos() != null) anamnesis.setHasTattoos(request.getHasTattoos());
        if (request.getUsesAspirin() != null) anamnesis.setUsesAspirin(request.getUsesAspirin());
        if (request.getHasKeloids() != null) anamnesis.setHasKeloids(request.getHasKeloids());
        if (request.getHasVitiligo() != null) anamnesis.setHasVitiligo(request.getHasVitiligo());
        if (request.getHasPsoriasis() != null) anamnesis.setHasPsoriasis(request.getHasPsoriasis());
        if (request.getHasDepression() != null) anamnesis.setHasDepression(request.getHasDepression());
        if (request.getDepressionControlled() != null)
            anamnesis.setDepressionControlled(request.getDepressionControlled());
        if (request.getHasAnemia() != null) anamnesis.setHasAnemia(request.getHasAnemia());
        if (request.getHadCancer() != null) anamnesis.setHadCancer(request.getHadCancer());
        if (request.getIsPregnant() != null) anamnesis.setIsPregnant(request.getIsPregnant());
        if (request.getIsBreastfeeding() != null) anamnesis.setIsBreastfeeding(request.getIsBreastfeeding());
        if (request.getHasDiabetes() != null) anamnesis.setHasDiabetes(request.getHasDiabetes());
        if (request.getHasHypertension() != null) anamnesis.setHasHypertension(request.getHasHypertension());
        if (request.getUsesCorticosteroids() != null)
            anamnesis.setUsesCorticosteroids(request.getUsesCorticosteroids());
        if (request.getUsedRoacutan() != null) anamnesis.setUsedRoacutan(request.getUsedRoacutan());
        if (request.getPracticesPhysicalActivity() != null)
            anamnesis.setPracticesPhysicalActivity(request.getPracticesPhysicalActivity());
        if (request.getActivityDescription() != null)
            anamnesis.setActivityDescription(request.getActivityDescription());
        if (request.getConsumesAlcohol() != null) anamnesis.setConsumesAlcohol(request.getConsumesAlcohol());
        if (request.getSmokes() != null) anamnesis.setSmokes(request.getSmokes());
        if (request.getUsesHormonalContraceptive() != null)
            anamnesis.setUsesHormonalContraceptive(request.getUsesHormonalContraceptive());
        if (request.getContraceptiveDescription() != null)
            anamnesis.setContraceptiveDescription(request.getContraceptiveDescription());
        if (request.getHasFoodAllergy() != null) anamnesis.setHasFoodAllergy(request.getHasFoodAllergy());
        if (request.getFoodAllergyDescription() != null)
            anamnesis.setFoodAllergyDescription(request.getFoodAllergyDescription());
        if (request.getNotes() != null) anamnesis.setNotes(request.getNotes());

        anamnesis.setUpdatedAt(LocalDateTime.now());
        return repository.save(anamnesis);
    }
}