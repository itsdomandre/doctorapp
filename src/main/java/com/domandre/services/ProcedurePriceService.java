package com.domandre.services;

import com.domandre.controllers.request.ProcedurePriceRequest;
import com.domandre.entities.ProcedurePrice;
import com.domandre.entities.User;
import com.domandre.enums.Procedures;
import com.domandre.exceptions.ResourceNotFoundException;
import com.domandre.repositories.ProcedurePriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProcedurePriceService {

    private final ProcedurePriceRepository procedurePriceRepository;

    public List<ProcedurePrice> findAll() {
        return procedurePriceRepository.findAll();
    }

    public ProcedurePrice findByProcedure(Procedures procedure) {
        return procedurePriceRepository.findByProcedure(procedure)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public Optional<ProcedurePrice> findByProcedureOptional(Procedures procedure) {
        return procedurePriceRepository.findByProcedure(procedure);
    }

    public ProcedurePrice upsert(ProcedurePriceRequest request, User admin) {
        ProcedurePrice price = procedurePriceRepository.findByProcedure(request.getProcedure())
                .orElseGet(ProcedurePrice::new);
        price.setProcedure(request.getProcedure());
        price.setPrice(request.getPrice());
        price.setUpdatedAt(LocalDateTime.now());
        price.setUpdatedBy(admin);
        return procedurePriceRepository.save(price);
    }
}
