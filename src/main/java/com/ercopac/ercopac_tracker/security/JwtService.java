package com.ercopac.ercopac_tracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;

    public JwtService(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 bytes for HS256");
        }
        this.secret = secret;
    }

    public String generateToken(
            Long userId,
            String username,
            String role,
            Long organisationId,
            String organisationName
    ) {
        return generateToken(userId, username, role, organisationId, organisationName, 6 * 60 * 60 * 1000L);
    }

    public String generateToken(
            Long userId,
            String username,
            String role,
            Long organisationId,
            String organisationName,
            long expirationMillis
    ) {
        long safeExpirationMillis = Math.max(60_000L, expirationMillis);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("organisationId", organisationId)
                .claim("organisationName", organisationName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + safeExpirationMillis))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object value = parseClaims(token).get("userId");
        return value == null ? null : Long.valueOf(value.toString());
    }

    public String extractRole(String token) {
        Object value = parseClaims(token).get("role");
        return value == null ? null : value.toString();
    }

    public Long extractOrganisationId(String token) {
        Object value = parseClaims(token).get("organisationId");
        return value == null ? null : Long.valueOf(value.toString());
    }

    public String extractOrganisationName(String token) {
        Object value = parseClaims(token).get("organisationName");
        return value == null ? null : value.toString();
    }
}
