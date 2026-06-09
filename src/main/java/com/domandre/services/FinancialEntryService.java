package com.domandre.services;

import com.domandre.controllers.request.CreatePayableRequest;
import com.domandre.controllers.request.RecordPaymentRequest;
import com.domandre.controllers.response.FinancialSummaryDTO;
import com.domandre.entities.Appointment;
import com.domandre.entities.FinancialEntry;
import com.domandre.enums.FinancialEntryStatus;
import com.domandre.enums.FinancialEntryType;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.repositories.FinancialEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class FinancialEntryService {

    private final FinancialEntryRepository financialEntryRepository;

    public FinancialEntry createReceivableFromAppointment(Appointment appointment, BigDecimal amount) {
        FinancialEntry entry = new FinancialEntry();
        entry.setType(FinancialEntryType.RECEIVABLE);
        entry.setStatus(FinancialEntryStatus.PENDING);
        entry.setAmount(amount);
        entry.setDescription(appointment.getProcedure().getLabel() + " — " +
                appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());
        entry.setDueDate(appointment.getAppointmentDate().toLocalDate());
        entry.setCreatedAt(LocalDateTime.now());
        entry.setAppointment(appointment);
        entry.setPatient(appointment.getPatient());
        return financialEntryRepository.save(entry);
    }

    public FinancialEntry createPayable(CreatePayableRequest request) {
        FinancialEntry entry = new FinancialEntry();
        entry.setType(FinancialEntryType.PAYABLE);
        entry.setStatus(FinancialEntryStatus.PENDING);
        entry.setAmount(request.getAmount());
        entry.setDescription(request.getDescription());
        entry.setDueDate(request.getDueDate());
        entry.setCreatedAt(LocalDateTime.now());
        return financialEntryRepository.save(entry);
    }

    public Page<FinancialEntry> findReceivables(FinancialEntryStatus status, LocalDate from, LocalDate to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return financialEntryRepository.findByFilters(FinancialEntryType.RECEIVABLE, status, from, to, pageable);
    }

    public BigDecimal sumReceivables(FinancialEntryStatus status, LocalDate from, LocalDate to) {
        return financialEntryRepository.sumAmountByFilters(FinancialEntryType.RECEIVABLE, status, from, to);
    }

    public Page<FinancialEntry> findPayables(FinancialEntryStatus status, LocalDate from, LocalDate to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return financialEntryRepository.findByFilters(FinancialEntryType.PAYABLE, status, from, to, pageable);
    }

    public BigDecimal sumPayables(FinancialEntryStatus status, LocalDate from, LocalDate to) {
        return financialEntryRepository.sumAmountByFilters(FinancialEntryType.PAYABLE, status, from, to);
    }

    public FinancialEntry recordPayment(Long id, RecordPaymentRequest request) {
        FinancialEntry entry = financialEntryRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
        if (entry.getStatus() != FinancialEntryStatus.PENDING) {
            throw new IllegalStateException("Only PENDING entries can be marked as paid");
        }
        entry.setStatus(FinancialEntryStatus.PAID);
        entry.setPaymentMethod(request.getPaymentMethod());
        entry.setPaidAt(LocalDateTime.now());
        return financialEntryRepository.save(entry);
    }

    public FinancialEntry cancel(Long id) {
        FinancialEntry entry = financialEntryRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
        if (entry.getStatus() != FinancialEntryStatus.PENDING) {
            throw new IllegalStateException("Only PENDING entries can be cancelled");
        }
        entry.setStatus(FinancialEntryStatus.CANCELLED);
        return financialEntryRepository.save(entry);
    }

    public FinancialSummaryDTO getSummary() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        BigDecimal totalReceivablePending = financialEntryRepository
                .sumAmountByTypeAndStatus(FinancialEntryType.RECEIVABLE, FinancialEntryStatus.PENDING);
        BigDecimal totalReceivablePaid = financialEntryRepository
                .sumAmountByTypeAndStatus(FinancialEntryType.RECEIVABLE, FinancialEntryStatus.PAID);
        BigDecimal totalPayablePending = financialEntryRepository
                .sumAmountByTypeAndStatus(FinancialEntryType.PAYABLE, FinancialEntryStatus.PENDING);
        BigDecimal totalPayablePaid = financialEntryRepository
                .sumAmountByTypeAndStatus(FinancialEntryType.PAYABLE, FinancialEntryStatus.PAID);

        BigDecimal netBalance = totalReceivablePaid.subtract(totalPayablePaid);

        long pendingReceivablesCount = financialEntryRepository
                .countByTypeAndStatus(FinancialEntryType.RECEIVABLE, FinancialEntryStatus.PENDING);
        long pendingPayablesCount = financialEntryRepository
                .countByTypeAndStatus(FinancialEntryType.PAYABLE, FinancialEntryStatus.PENDING);

        BigDecimal receivablePaidThisMonth = financialEntryRepository
                .sumAmountByTypeAndStatusAndDueDateBetween(
                        FinancialEntryType.RECEIVABLE, FinancialEntryStatus.PAID, monthStart, monthEnd);
        BigDecimal payablePaidThisMonth = financialEntryRepository
                .sumAmountByTypeAndStatusAndDueDateBetween(
                        FinancialEntryType.PAYABLE, FinancialEntryStatus.PAID, monthStart, monthEnd);

        return new FinancialSummaryDTO(
                totalReceivablePending,
                totalReceivablePaid,
                totalPayablePending,
                totalPayablePaid,
                netBalance,
                pendingReceivablesCount,
                pendingPayablesCount,
                receivablePaidThisMonth,
                payablePaidThisMonth
        );
    }
}
