package com.domandre.entities;

import com.domandre.enums.AppointmentStatus;
import com.domandre.enums.Procedures;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appointments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore
    private User patient;

    @Column(name = "dateTime")
    private LocalDateTime dateTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.REQUESTED;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Procedures procedure;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "anamnesis_id")
    private Anamnesis anamnesis;

    private LocalDateTime updatedAt;
    private String notes;
}
