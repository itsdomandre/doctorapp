package com.domandre.repositories;

import com.domandre.entities.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, UUID> {
    List<ConfirmationToken> findByUserIdAndValidTrue(UUID userId);
}