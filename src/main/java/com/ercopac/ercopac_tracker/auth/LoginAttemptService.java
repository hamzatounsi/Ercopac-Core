package com.ercopac.ercopac_tracker.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final ConcurrentMap<String, AttemptState> attempts = new ConcurrentHashMap<>();
    private final int maxFailedAttempts;
    private final Duration lockDuration;
    private final Clock clock;

    public LoginAttemptService(
            @Value("${auth.login.max-failed-attempts:5}") int maxFailedAttempts,
            @Value("${auth.login.lock-minutes:15}") long lockMinutes
    ) {
        this.maxFailedAttempts = Math.max(1, maxFailedAttempts);
        Duration configuredLockDuration = Duration.ofMinutes(lockMinutes);
        this.lockDuration = configuredLockDuration.isNegative() || configuredLockDuration.isZero()
                ? Duration.ofMinutes(15)
                : configuredLockDuration;
        this.clock = Clock.systemUTC();
    }

    public void assertNotLocked(String username) {
        String key = key(username);
        AttemptState state = attempts.get(key);

        if (state == null || state.lockedUntil == null) {
            return;
        }

        Instant now = Instant.now(clock);
        if (state.lockedUntil.isAfter(now)) {
            throw new LoginTemporarilyLockedException(state.lockedUntil);
        }

        attempts.remove(key, state);
    }

    public void recordFailure(String username, String ipAddress) {
        recordFailure(username, ipAddress, maxFailedAttempts);
    }

    public void recordFailure(String username, String ipAddress, int configuredMaxFailedAttempts) {
        String key = key(username);
        int effectiveMaxFailedAttempts = Math.max(1, configuredMaxFailedAttempts);
        AttemptState updated = attempts.compute(key, (ignored, current) -> {
            Instant now = Instant.now(clock);
            AttemptState next = current == null ? new AttemptState() : current;

            if (next.lockedUntil != null && next.lockedUntil.isAfter(now)) {
                return next;
            }

            next.failedAttempts++;
            if (next.failedAttempts >= effectiveMaxFailedAttempts) {
                next.lockedUntil = now.plus(lockDuration);
                log.warn("Login temporarily locked for username={} ip={} until={}", key, safeIp(ipAddress), next.lockedUntil);
            } else {
                log.warn("Failed login attempt for username={} ip={} failedAttempts={}", key, safeIp(ipAddress), next.failedAttempts);
            }
            return next;
        });

        if (updated.lockedUntil != null && updated.lockedUntil.isAfter(Instant.now(clock))) {
            throw new LoginTemporarilyLockedException(updated.lockedUntil);
        }
    }

    public void recordSuccess(String username, String ipAddress) {
        String key = key(username);
        attempts.remove(key);
        log.info("Successful login for username={} ip={}", key, safeIp(ipAddress));
    }

    private String key(String username) {
        if (username == null || username.isBlank()) {
            return "anonymous";
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String safeIp(String ipAddress) {
        return ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress;
    }

    private static class AttemptState {
        private int failedAttempts;
        private Instant lockedUntil;
    }
}
