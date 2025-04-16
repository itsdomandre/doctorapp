package com.domandre.repositories;

import com.domandre.entities.Anamnesis;
import com.domandre.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnamnesisRepository extends JpaRepository<Anamnesis, Long> {
    List<Anamnesis> findAllByPatientId(UUID patientId);

    boolean existsByPatientAndCreatedAtAfter(User patient, LocalDateTime window);
}