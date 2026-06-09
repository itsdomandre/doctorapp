package com.domandre.enums;

public enum FinancialEntryStatus {
    PENDING,
    PAID,
    CANCELLED;

    public String getLabel() {
        return switch (this) {
            case PENDING -> "Pendente";
            case PAID -> "Pago";
            case CANCELLED -> "Cancelado";
        };
    }
}
