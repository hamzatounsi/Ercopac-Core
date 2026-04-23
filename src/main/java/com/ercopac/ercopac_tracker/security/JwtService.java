package com.ercopac.ercopac_tracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET =
            "CHANGE_ME_TO_A_LONG_RANDOM_SECRET_KEY_1234567890";

    public String generateToken(Long userId, String username, String role, Long organisationId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("organisationId", organisationId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 6))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
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
}