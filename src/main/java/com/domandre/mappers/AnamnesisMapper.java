package com.domandre.mappers;

import com.domandre.controllers.response.AnamnesisDTO;
import com.domandre.controllers.request.AnamnesisRequest;
import com.domandre.entities.Anamnesis;

import java.time.LocalDateTime;

public class AnamnesisMapper {
    public static Anamnesis fromRequest(AnamnesisRequest request) {
        return Anamnesis.builder() 
                .hasChronicDisease(request.getHasChronicDisease())
                .chronicDiseaseDescription(request.getChronicDiseaseDescription())
                .usesContinuousMedication(request.getUsesContinuousMedication())
                .medicationDescription(request.getMedicationDescription())
                .hasAllergies(request.getHasAllergies())
                .allergyDescription(request.getAllergyDescription())
                .hadAestheticTreatment(request.getHadAestheticTreatment())
                .treatmentDescription(request.getTreatmentDescription())
                .hasTattoos(request.getHasTattoos())
                .usesAspirin(request.getUsesAspirin())
                .hasKeloids(request.getHasKeloids())
                .hasVitiligo(request.getHasVitiligo())
                .hasPsoriasis(request.getHasPsoriasis())
                .hasDepression(request.getHasDepression())
                .depressionControlled(request.getDepressionControlled())
                .hasAnemia(request.getHasAnemia())
                .hadCancer(request.getHadCancer())
                .isPregnant(request.getIsPregnant())
                .isBreastfeeding(request.getIsBreastfeeding())
                .hasDiabetes(request.getHasDiabetes())
                .hasHypertension(request.getHasHypertension())
                .usesCorticosteroids(request.getUsesCorticosteroids())
                .usedRoacutan(request.getUsedRoacutan())
                .practicesPhysicalActivity(request.getPracticesPhysicalActivity())
                .activityDescription(request.getActivityDescription())
                .consumesAlcohol(request.getConsumesAlcohol())
                .smokes(request.getSmokes())
                .usesHormonalContraceptive(request.getUsesHormonalContraceptive())
                .contraceptiveDescription(request.getContraceptiveDescription())
                .hasFoodAllergy(request.getHasFoodAllergy())
                .foodAllergyDescription(request.getFoodAllergyDescription())
                .createdAt(LocalDateTime.now())
                .notes(request.getNotes())
                .build();
    }

    public static AnamnesisDTO toDTO(Anamnesis ficha) {
        AnamnesisDTO dto = new AnamnesisDTO();
        dto.setId(ficha.getId());
        dto.setCreatedAt(ficha.getCreatedAt());
        dto.setHasChronicDisease(ficha.getHasChronicDisease());
        dto.setChronicDiseaseDescription(ficha.getChronicDiseaseDescription());
        dto.setUsesContinuousMedication(ficha.getUsesContinuousMedication());
        dto.setMedicationDescription(ficha.getMedicationDescription());
        dto.setHasAllergies(ficha.getHasAllergies());
        dto.setAllergyDescription(ficha.getAllergyDescription());
        dto.setHadAestheticTreatment(ficha.getHadAestheticTreatment());
        dto.setTreatmentDescription(ficha.getTreatmentDescription());
        dto.setHasTattoos(ficha.getHasTattoos());
        dto.setUsesAspirin(ficha.getUsesAspirin());
        dto.setHasKeloids(ficha.getHasKeloids());
        dto.setHasVitiligo(ficha.getHasVitiligo());
        dto.setHasPsoriasis(ficha.getHasPsoriasis());
        dto.setHasDepression(ficha.getHasDepression());
        dto.setDepressionControlled(ficha.getDepressionControlled());
        dto.setHasAnemia(ficha.getHasAnemia());
        dto.setHadCancer(ficha.getHadCancer());
        dto.setIsPregnant(ficha.getIsPregnant());
        dto.setIsBreastfeeding(ficha.getIsBreastfeeding());
        dto.setHasDiabetes(ficha.getHasDiabetes());
        dto.setHasHypertension(ficha.getHasHypertension());
        dto.setUsesCorticosteroids(ficha.getUsesCorticosteroids());
        dto.setUsedRoacutan(ficha.getUsedRoacutan());
        dto.setPracticesPhysicalActivity(ficha.getPracticesPhysicalActivity());
        dto.setActivityDescription(ficha.getActivityDescription());
        dto.setConsumesAlcohol(ficha.getConsumesAlcohol());
        dto.setSmokes(ficha.getSmokes());
        dto.setUsesHormonalContraceptive(ficha.getUsesHormonalContraceptive());
        dto.setContraceptiveDescription(ficha.getContraceptiveDescription());
        dto.setHasFoodAllergy(ficha.getHasFoodAllergy());
        dto.setFoodAllergyDescription(ficha.getFoodAllergyDescription());
        dto.setNotes(ficha.getNotes());
        return dto;
    }
}
