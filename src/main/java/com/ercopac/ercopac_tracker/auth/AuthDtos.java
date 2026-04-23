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
            String organisationName
    ) {}
}