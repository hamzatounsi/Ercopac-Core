package com.ercopac.ercopac_tracker.auth;

import java.time.Instant;

public class LoginTemporarilyLockedException extends RuntimeException {

    private final Instant lockedUntil;

    public LoginTemporarilyLockedException(Instant lockedUntil) {
        super("Too many failed login attempts. Please try again later.");
        this.lockedUntil = lockedUntil;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }
}
