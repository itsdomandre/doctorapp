package com.domandre.controllers;

import com.domandre.controllers.request.ProcedurePriceRequest;
import com.domandre.controllers.response.ProcedurePriceDTO;
import com.domandre.entities.User;
import com.domandre.mappers.ProcedurePriceMapper;
import com.domandre.services.ProcedurePriceService;
import com.domandre.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/procedure-prices")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class ProcedurePriceController {

    private final ProcedurePriceService procedurePriceService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ProcedurePriceDTO>> getAll() {
        List<ProcedurePriceDTO> prices = procedurePriceService.findAll()
                .stream()
                .map(ProcedurePriceMapper::toDTO)
                .toList();
        return ResponseEntity.ok(prices);
    }

    @PutMapping
    public ResponseEntity<ProcedurePriceDTO> upsert(@Valid @RequestBody ProcedurePriceRequest request) {
        User admin = userService.getCurrentUser();
        log.info("ADMIN {} updating price for procedure: {}", admin.getEmail(), request.getProcedure());
        return ResponseEntity.ok(ProcedurePriceMapper.toDTO(procedurePriceService.upsert(request, admin)));
    }
}
