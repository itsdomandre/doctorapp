package com.domandre.repositories;

import com.domandre.entities.ProcedurePrice;
import com.domandre.enums.Procedures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcedurePriceRepository extends JpaRepository<ProcedurePrice, Long> {
    Optional<ProcedurePrice> findByProcedure(Procedures procedure);
}
