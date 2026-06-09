package com.domandre.enums;

public enum FinancialEntryType {
    RECEIVABLE,
    PAYABLE;

    public String getLabel() {
        return switch (this) {
            case RECEIVABLE -> "Conta a receber";
            case PAYABLE -> "Conta a pagar";
        };
    }
}
