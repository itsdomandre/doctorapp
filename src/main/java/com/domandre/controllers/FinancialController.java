package com.domandre.controllers;

import com.domandre.controllers.request.CreatePayableRequest;
import com.domandre.controllers.request.RecordPaymentRequest;
import com.domandre.controllers.response.FinancialEntryDTO;
import com.domandre.controllers.response.FinancialSummaryDTO;
import com.domandre.enums.FinancialEntryStatus;
import com.domandre.mappers.FinancialEntryMapper;
import com.domandre.services.FinancialEntryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/financial")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class FinancialController {

    private final FinancialEntryService financialEntryService;

    @GetMapping("/summary")
    public ResponseEntity<FinancialSummaryDTO> getSummary() {
        return ResponseEntity.ok(financialEntryService.getSummary());
    }

    @GetMapping("/receivables")
    public ResponseEntity<Page<FinancialEntryDTO>> getReceivables(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) FinancialEntryStatus status) {
        return ResponseEntity.ok(financialEntryService.findReceivables(status, page, size)
                .map(FinancialEntryMapper::toDTO));
    }

    @GetMapping("/payables")
    public ResponseEntity<Page<FinancialEntryDTO>> getPayables(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) FinancialEntryStatus status) {
        return ResponseEntity.ok(financialEntryService.findPayables(status, page, size)
                .map(FinancialEntryMapper::toDTO));
    }

    @PostMapping("/payables")
    public ResponseEntity<FinancialEntryDTO> createPayable(@Valid @RequestBody CreatePayableRequest request) {
        log.info("Creating manual payable: {}", request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FinancialEntryMapper.toDTO(financialEntryService.createPayable(request)));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<FinancialEntryDTO> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody RecordPaymentRequest request) {
        log.info("Recording payment for entry ID={}", id);
        return ResponseEntity.ok(FinancialEntryMapper.toDTO(financialEntryService.recordPayment(id, request)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<FinancialEntryDTO> cancel(@PathVariable Long id) {
        log.info("Cancelling financial entry ID={}", id);
        return ResponseEntity.ok(FinancialEntryMapper.toDTO(financialEntryService.cancel(id)));
    }
}
