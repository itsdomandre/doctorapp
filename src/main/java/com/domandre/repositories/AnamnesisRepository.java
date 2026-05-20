package com.domandre.repositories;

import com.domandre.entities.Anamnesis;
import com.domandre.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnamnesisRepository extends JpaRepository<Anamnesis, Long> {
    List<Anamnesis> findAllByPatientId(UUID patientId);
    Anamnesis findTopByPatientIdOrderByCreatedAtDesc(UUID patientId);

    @Query("SELECT a FROM Anamnesis a WHERE a.patient.id = :patientId " +
           "AND (:start IS NULL OR a.createdAt >= :start) " +
           "AND (:end IS NULL OR a.createdAt <= :end) " +
           "ORDER BY a.createdAt DESC")
    List<Anamnesis> findByPatientIdAndDateRange(
            @Param("patientId") UUID patientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}