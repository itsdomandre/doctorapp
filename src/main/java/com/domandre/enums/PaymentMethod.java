package com.domandre.enums;

public enum PaymentMethod {
    DINHEIRO,
    CARTAO_DEBITO,
    CARTAO_CREDITO,
    PIX;

    public String getLabel() {
        return switch (this) {
            case DINHEIRO -> "Dinheiro";
            case CARTAO_DEBITO -> "Cartão de débito";
            case CARTAO_CREDITO -> "Cartão de crédito";
            case PIX -> "PIX";
        };
    }
}
