package com.domandre.mappers;

import com.domandre.controllers.response.ProcedurePriceDTO;
import com.domandre.entities.ProcedurePrice;

public class ProcedurePriceMapper {

    public static ProcedurePriceDTO toDTO(ProcedurePrice entity) {
        ProcedurePriceDTO dto = new ProcedurePriceDTO();
        dto.setId(entity.getId());
        dto.setProcedure(entity.getProcedure());
        dto.setProcedureLabel(entity.getProcedure().getLabel());
        dto.setPrice(entity.getPrice());
        dto.setUpdatedAt(entity.getUpdatedAt());
        if (entity.getUpdatedBy() != null) {
            dto.setUpdatedBy(entity.getUpdatedBy().getFirstName() + " " + entity.getUpdatedBy().getLastName());
        }
        return dto;
    }
}
