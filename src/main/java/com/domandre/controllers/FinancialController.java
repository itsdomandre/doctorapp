package com.domandre.controllers;

import com.domandre.controllers.request.CreatePayableRequest;
import com.domandre.controllers.request.RecordPaymentRequest;
import com.domandre.controllers.response.FinancialEntryDTO;
import com.domandre.controllers.response.FinancialEntryPageResponse;
import com.domandre.controllers.response.FinancialSummaryDTO;
import com.domandre.enums.FinancialEntryStatus;
import com.domandre.mappers.FinancialEntryMapper;
import com.domandre.services.FinancialEntryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/financial")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class FinancialController {

    private final FinancialEntryService financialEntryService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialSummaryDTO> getSummary() {
        return ResponseEntity.ok(financialEntryService.getSummary());
    }

    @GetMapping("/receivables")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialEntryPageResponse> getReceivables(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) FinancialEntryStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo) {
        Page<FinancialEntryDTO> entries = financialEntryService
                .findReceivables(status, dueDateFrom, dueDateTo, page, size)
                .map(FinancialEntryMapper::toDTO);
        return ResponseEntity.ok(new FinancialEntryPageResponse(
                entries.getContent(), entries.getTotalElements(), entries.getTotalPages(),
                entries.getNumber(), entries.getSize(),
                financialEntryService.sumReceivables(status, dueDateFrom, dueDateTo)));
    }

    @GetMapping("/payables")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialEntryPageResponse> getPayables(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) FinancialEntryStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo) {
        Page<FinancialEntryDTO> entries = financialEntryService
                .findPayables(status, dueDateFrom, dueDateTo, page, size)
                .map(FinancialEntryMapper::toDTO);
        return ResponseEntity.ok(new FinancialEntryPageResponse(
                entries.getContent(), entries.getTotalElements(), entries.getTotalPages(),
                entries.getNumber(), entries.getSize(),
                financialEntryService.sumPayables(status, dueDateFrom, dueDateTo)));
    }

    @PostMapping("/payables")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialEntryDTO> createPayable(@Valid @RequestBody CreatePayableRequest request) {
        log.info("Creating manual payable: {}", request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FinancialEntryMapper.toDTO(financialEntryService.createPayable(request)));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialEntryDTO> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody RecordPaymentRequest request) {
        log.info("Recording payment for entry ID={}", id);
        return ResponseEntity.ok(FinancialEntryMapper.toDTO(financialEntryService.recordPayment(id, request)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialEntryDTO> cancel(@PathVariable Long id) {
        log.info("Cancelling financial entry ID={}", id);
        return ResponseEntity.ok(FinancialEntryMapper.toDTO(financialEntryService.cancel(id)));
    }
}
