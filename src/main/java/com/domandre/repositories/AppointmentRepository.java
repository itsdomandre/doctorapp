package com.domandre.repositories;

import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(User patient);

    List<Appointment> findAllByAppointmentDateBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);
}
