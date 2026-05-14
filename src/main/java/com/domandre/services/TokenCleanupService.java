package com.domandre.services;

import com.domandre.repositories.InvalidTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final InvalidTokenRepository invalidTokenRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void purgeExpiredTokens() {
        invalidTokenRepository.deleteByExpirationBefore(new Date());
        log.info("Purged expired tokens from blacklist");
    }
}
