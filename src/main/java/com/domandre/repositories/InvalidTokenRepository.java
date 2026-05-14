package com.domandre.repositories;

import com.domandre.entities.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidToken, String> {
    boolean existsByToken(String token);

    @Transactional
    void deleteByExpirationBefore(Date cutoff);
}
