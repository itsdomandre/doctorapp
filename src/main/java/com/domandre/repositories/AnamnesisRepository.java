package com.domandre.repositories;

import com.domandre.entities.Anamnesis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnamnesisRepository extends JpaRepository<Anamnesis, Long> {

}