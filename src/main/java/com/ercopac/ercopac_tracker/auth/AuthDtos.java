package com.ercopac.ercopac_tracker.auth;

public class AuthDtos {

    public record LoginRequest(String username, String password) {}

    public record LoginResponse(
            String token,
            Long userId,
            String email,
            String role,
            Long organisationId,
            String organisationCode,
            String organisationName,
            boolean passwordResetRequired,
            String resetStatus,
            String resetToken,
            String message
    ) {}

    public record ForgotPasswordRequest(String email) {}

    public record MessageResponse(String message) {}

    public record ResetPasswordRequest(String token, String newPassword) {}

    public record PasswordResetDecisionRequest(String note) {}

    public record CheckApprovedResetRequest(String email) {}

    public record CheckApprovedResetResponse(
            boolean approved,
            String token,
            String message
    ) {}
}