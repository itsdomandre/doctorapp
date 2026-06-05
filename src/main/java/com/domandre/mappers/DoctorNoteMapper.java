package com.domandre.mappers;

import com.domandre.controllers.response.DoctorNoteDTO;
import com.domandre.entities.DoctorNote;

public class DoctorNoteMapper {

    public static DoctorNoteDTO toDTO(DoctorNote note) {
        DoctorNoteDTO dto = new DoctorNoteDTO();
        dto.setId(note.getId());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        if (note.getDoctor() != null) {
            dto.setDoctorName(note.getDoctor().getFirstName() + " " + note.getDoctor().getLastName());
        }
        return dto;
    }
}
