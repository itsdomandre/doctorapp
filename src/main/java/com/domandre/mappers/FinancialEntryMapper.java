package com.domandre.mappers;

import com.domandre.controllers.response.FinancialEntryDTO;
import com.domandre.entities.FinancialEntry;

public class FinancialEntryMapper {

    public static FinancialEntryDTO toDTO(FinancialEntry entry) {
        FinancialEntryDTO dto = new FinancialEntryDTO();
        dto.setId(entry.getId());
        dto.setType(entry.getType());
        dto.setTypeLabel(entry.getType().getLabel());
        dto.setStatus(entry.getStatus());
        dto.setStatusLabel(entry.getStatus().getLabel());
        dto.setAmount(entry.getAmount());
        dto.setDescription(entry.getDescription());
        dto.setDueDate(entry.getDueDate());
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setPaymentMethod(entry.getPaymentMethod());
        if (entry.getPaymentMethod() != null) {
            dto.setPaymentMethodLabel(entry.getPaymentMethod().getLabel());
        }
        dto.setPaidAt(entry.getPaidAt());
        if (entry.getAppointment() != null) {
            dto.setAppointmentId(entry.getAppointment().getId());
        }
        if (entry.getPatient() != null) {
            dto.setPatientName(entry.getPatient().getFirstName() + " " + entry.getPatient().getLastName());
        }
        return dto;
    }
}
