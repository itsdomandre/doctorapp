package com.domandre.enums;

public enum Procedures {
    AVALIACAO_CLINICA,
    DESINTOXICACAO,
    HARMONIZACAO_FACIAL,
    PREENCHIMENTO_FACIAL,
    LASER_LAVIEEN,
    LIMPEZA_DE_PELE,
    PEELING_QUIMICO,
    SUPLEMENTACAO_VITAMINA_INJETAVEL,
    TERAPIA_CAPILAR,
    TOXINA_BOTULINICA,
    ULTRAFORMER;

    public String getLabel() {
        return switch (this) {
            case PEELING_QUIMICO -> "Peeling químico";
            case SUPLEMENTACAO_VITAMINA_INJETAVEL -> "Suplementação de vitamina injetável";
            case TOXINA_BOTULINICA -> "Toxina botulínica";
            case PREENCHIMENTO_FACIAL -> "Preenchimento facial";
            case HARMONIZACAO_FACIAL -> "Harmonização facial";
            case TERAPIA_CAPILAR -> "Terapia capilar";
            case DESINTOXICACAO -> "Desintoxicação";
            case LIMPEZA_DE_PELE -> "Limpeza de pele";
            case AVALIACAO_CLINICA -> "Avaliação clínica";
            case LASER_LAVIEEN -> "Laser Lavieen";
            case ULTRAFORMER -> "Ultraformer";
        };
    }
}

