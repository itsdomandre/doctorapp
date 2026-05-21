package com.domandre.repositories;

import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(User patient);
    Page<Appointment> findByPatient(User patient, Pageable pageable);
    Page<Appointment> findByPatientAndStatus(User patient, AppointmentStatus status, Pageable pageable);
    List<Appointment> findAllByAppointmentDateBetween(LocalDateTime from, LocalDateTime to);
    Page<Appointment> findAllByAppointmentDateBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    @Query("SELECT a FROM Appointment a WHERE a.status = 'REQUESTED'")
    List<Appointment> findAllRequested();
    boolean existsByAppointmentDate(LocalDateTime appointmentDate);

    // patientName is intentionally excluded from this query: first_name/last_name
    // are stored as bytea (encrypted at the DB level), so LOWER() fails at the
    // PostgreSQL type-check stage even when the parameter is null. Name filtering
    // is applied in-memory by AppointmentService after Hibernate deserialises the fields.
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.appointmentDate BETWEEN :from AND :to
            AND (:patientId IS NULL OR a.patient.id = :patientId)
            AND (:status IS NULL OR a.status = :status)
            """)
    List<Appointment> search(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("patientId") UUID patientId,
            @Param("status") AppointmentStatus status
    );
}
