package com.domandre.repositories;

import com.domandre.entities.AppointmentMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentMessageRepository extends JpaRepository<AppointmentMessage, Long> {
}
