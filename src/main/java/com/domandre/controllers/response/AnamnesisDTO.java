package com.domandre.DTOs;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnamnesisDTO {
    private Long id;
    private LocalDateTime createdAt;
    private Boolean hasChronicDisease;
    private String chronicDiseaseDescription;
    private Boolean usesContinuousMedication;
    private String medicationDescription;
    private Boolean hasAllergies;
    private String allergyDescription;
    private Boolean hadAestheticTreatment;
    private String treatmentDescription;
    private Boolean hasTattoos;
    private Boolean usesAspirin;
    private Boolean hasKeloids;
    private Boolean hasVitiligo;
    private Boolean hasPsoriasis;
    private Boolean hasDepression;
    private Boolean depressionControlled;
    private Boolean hasAnemia;
    private Boolean hadCancer;
    private Boolean isPregnant;
    private Boolean isBreastfeeding;
    private Boolean hasDiabetes;
    private Boolean hasHypertension;
    private Boolean usesCorticosteroids;
    private Boolean usedRoacutan;
    private Boolean practicesPhysicalActivity;
    private String activityDescription;
    private Boolean consumesAlcohol;
    private Boolean smokes;
    private Boolean usesHormonalContraceptive;
    private String contraceptiveDescription;
    private Boolean hasFoodAllergy;
    private String foodAllergyDescription;
    private LocalDateTime updatedAt;
    private String notes;
}

