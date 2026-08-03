package com.ercopac.ercopac_tracker.auth.passwordreset;

import java.time.LocalDateTime;

public record PasswordResetRequestDto(
        Long id,
        Long userId,
        String fullName,
        String email,
        String status,
        LocalDateTime requestedAt
) {}
