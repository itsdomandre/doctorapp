package com.domandre.repositories;

import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(User patient);

    List<Appointment> findAllByAppointmentDateBetween(LocalDateTime from, LocalDateTime to);// DateBetween must be 2 arguments (from, to) JPA+DB characteristic

    @Query("SELECT a FROM Appointment a WHERE a.status = 'REQUESTED'")
    List<Appointment> findAllRequested();

    boolean existsByAppointmentDate (LocalDateTime appointmentDate);
}
