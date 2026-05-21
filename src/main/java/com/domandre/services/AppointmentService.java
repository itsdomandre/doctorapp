package com.domandre.services;

import com.domandre.controllers.request.AppointmentRequest;
import com.domandre.controllers.request.AppointmentSearchRequest;
import com.domandre.entities.Appointment;
import com.domandre.entities.User;
import com.domandre.enums.AppointmentStatus;
import com.domandre.enums.Role;
import com.domandre.exceptions.AdminMustBeProvidedException;
import com.domandre.exceptions.AppointmentNotCancellableException;
import com.domandre.exceptions.DateTimeRequestIsNotPermittedException;
import com.domandre.exceptions.InsufficientPermissionsException;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.helpers.BusinessHoursHelper;
import com.domandre.helpers.BusinessHoursHelper.TimeRange;
import com.domandre.repositories.AppointmentRepository;
import com.domandre.validators.AppointmentValidator;
import com.domandre.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public Appointment createAppointment(AppointmentRequest request) throws DateTimeRequestIsNotPermittedException {
        if (!AppointmentValidator.isValidAppointment(request.getDateTime())) {
            throw new DateTimeRequestIsNotPermittedException();
        }
        User patient = userService.getCurrentUser();
        if (appointmentRepository.existsByAppointmentDate(request.getDateTime())) {
            throw new DateTimeRequestIsNotPermittedException();
        }
        Appointment appointment = Appointment.builder().
                patient(patient)
                .appointmentDate(request.getDateTime())
                .procedure(request.getProcedure())
                .notes(request.getNotes()).status(AppointmentStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();
        return appointmentRepository.save(appointment);
    }

    public Page<Appointment> getAllAppointments(int page, int size) {
        return appointmentRepository.findAll(PageRequest.of(page, size, Sort.by("appointmentDate").descending()));
    }

    public Appointment getOrThrow(Long id) throws ResourceNotFoundException {
        return appointmentRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public Page<Appointment> getAppointmentsForCurrentUser(User currentUser, AppointmentStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("appointmentDate").descending());
        if (status != null) {
            return appointmentRepository.findByPatientAndStatus(currentUser, status, pageable);
        }
        return appointmentRepository.findByPatient(currentUser, pageable);
    }

    public Appointment updateAppointmentStatus(Long id, AppointmentStatus newStatus, UUID doctorId) throws AdminMustBeProvidedException, InsufficientPermissionsException, ResourceNotFoundException {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(ResourceNotFoundException::new);

        appointment.setStatus(newStatus);
        if (newStatus == AppointmentStatus.APPROVED) {
            if (doctorId == null) {
                throw new AdminMustBeProvidedException();
            }
            User doctor = userRepository.findById(doctorId).orElseThrow(ResourceNotFoundException::new);
            if (doctor.getRole() != Role.ADMIN) {
                throw new InsufficientPermissionsException();
            }
            appointment.setDoctor(doctor);
            appointment.setUpdatedAt(LocalDateTime.now());
        }
        return appointmentRepository.save(appointment);
    }

    public List<LocalTime> getAvailableSlots(LocalDate date) {
        Optional<TimeRange> rangeOptional = BusinessHoursHelper.getBusinessHours(date.getDayOfWeek());
        if (rangeOptional.isEmpty()) {
            return List.of();
        }
        TimeRange range = rangeOptional.get();
        LocalTime start = range.start();
        LocalTime end = range.end().minusHours(1);

        List<LocalTime> allPossibleSlots = new ArrayList<>();
        for (LocalTime time = start; !time.isAfter(end); time = time.plusHours(1)) {
            allPossibleSlots.add(time);
        }

        List<LocalDateTime> taken = appointmentRepository.findAllByAppointmentDateBetween(date.atStartOfDay(), date.atTime(23, 59)).stream().map(Appointment::getAppointmentDate).toList();
        return allPossibleSlots.stream().filter(slot -> {
            LocalDateTime dateTime = LocalDateTime.of(date, slot);
            if (dateTime.isBefore(LocalDateTime.now())) return false;
            boolean alreadyTaken = taken.stream().anyMatch(t -> t.toLocalTime().equals(slot));

            return !alreadyTaken;
        }).collect(Collectors.toList());
    }

    public List<Appointment> searchAppointments(AppointmentSearchRequest request) {
        LocalDate from = request.getFromDate() != null ? request.getFromDate() : LocalDate.now().minusMonths(1);
        LocalDate to = request.getToDate() != null ? request.getToDate() : LocalDate.now().plusMonths(1);

        return appointmentRepository.search(
                from.atStartOfDay(),
                to.atTime(23, 59),
                request.getPatientId(),
                request.getStatus(),
                request.getPatientName()
        );
    }

    public Page<Appointment> getTodayAppointments(int page, int size) {
        LocalDate today = LocalDate.now();
        return appointmentRepository.findAllByAppointmentDateBetween(
                today.atStartOfDay(), today.atTime(23, 59),
                PageRequest.of(page, size, Sort.by("appointmentDate").ascending())
        );
    }

    public Appointment cancelAppointment(Long id) throws ResourceNotFoundException, InsufficientPermissionsException, AppointmentNotCancellableException {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        User currentUser = userService.getCurrentUser();
        if (!appointment.getPatient().getId().equals(currentUser.getId())) {
            throw new InsufficientPermissionsException();
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.REJECTED) {
            throw new AppointmentNotCancellableException();
        }
        if (appointment.getAppointmentDate().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new AppointmentNotCancellableException();
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getPendingAppointments() {
        return appointmentRepository.findAllRequested();
    }

    public Map<String, Long> getMonthlyAvailability(YearMonth yearMonth) {
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        List<LocalDateTime> takenSlots = appointmentRepository
                .findAllByAppointmentDateBetween(firstDay.atStartOfDay(), lastDay.atTime(23, 59))
                .stream()
                .map(Appointment::getAppointmentDate)
                .toList();

        Map<String, Long> availability = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            Optional<TimeRange> rangeOpt = BusinessHoursHelper.getBusinessHours(date.getDayOfWeek());
            if (rangeOpt.isEmpty()) continue;

            TimeRange range = rangeOpt.get();
            LocalTime start = range.start();
            LocalTime end = range.end().minusHours(1);

            long count = 0;
            for (LocalTime time = start; !time.isAfter(end); time = time.plusHours(1)) {
                LocalDateTime slot = LocalDateTime.of(date, time);
                if (slot.isBefore(now)) continue;
                if (!takenSlots.contains(slot)) count++;
            }

            if (count > 0) {
                availability.put(date.toString(), count);
            }
        }

        return availability;
    }
}
