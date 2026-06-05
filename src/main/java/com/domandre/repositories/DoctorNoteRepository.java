package com.domandre.repositories;

import com.domandre.entities.DoctorNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorNoteRepository extends JpaRepository<DoctorNote, Long> {
}
