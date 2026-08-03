package com.ercopac.ercopac_tracker.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ercopac.ercopac_tracker.organisation.domain.OrganisationStatus;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        if (path.equals("/api/auth/password-reset/pending")
                || path.matches("/api/auth/password-reset/\\d+/(approve|reject)")) {
            return false;
        }

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/password-reset/request")
                || path.equals("/api/auth/password-reset/reset")
                || path.equals("/api/health")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            String username = jwtService.extractUsername(token);

            if (username == null || username.isBlank()) {
                chain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                AppUser user = userRepository.findByEmail1(username)
                        .orElse(null);

                if (!isCurrentAccountStateValid(token, user)) {
                    sendUnauthorized(response, "Authentication is no longer valid");
                    return;
                }

                String cleanRole = user.getRole().name();
                Long organisationId = user.getOrganisation() == null
                        ? null
                        : user.getOrganisation().getId();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(cleanRole),
                                        new SimpleGrantedAuthority("ROLE_" + cleanRole)
                                )
                        );

                Map<String, Object> details = new HashMap<>();
                details.put("userId", user.getId());
                details.put("organisationId", organisationId);
                details.put("role", cleanRole);

                authToken.setDetails(details);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            sendUnauthorized(response, "JWT expired");
        } catch (JwtException e) {
            sendUnauthorized(response, "Invalid JWT");
        } catch (Exception e) {
            sendUnauthorized(response, "Authentication failed");
        }
    }

    private boolean isCurrentAccountStateValid(String token, AppUser user) {
        if (user == null || !user.isActive() || user.getRole() == null) {
            return false;
        }

        if (user.getRole() != Role.PLATFORM_OWNER && user.getOrganisation() == null) {
            return false;
        }

        Long tokenUserId = jwtService.extractUserId(token);
        String tokenRole = jwtService.extractRole(token);
        Long tokenOrganisationId = jwtService.extractOrganisationId(token);
        Long currentOrganisationId = user.getOrganisation() == null
                ? null
                : user.getOrganisation().getId();
        String cleanTokenRole = tokenRole == null
                ? null
                : tokenRole.replaceFirst("^ROLE_", "");

        if (!user.getId().equals(tokenUserId)
                || !user.getRole().name().equals(cleanTokenRole)
                || !java.util.Objects.equals(currentOrganisationId, tokenOrganisationId)) {
            return false;
        }

        return user.getOrganisation() == null
                || (user.getOrganisation().isActive()
                && user.getOrganisation().getStatus() != OrganisationStatus.SUSPENDED);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();

        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"timestamp\":\"" + Instant.now()
                            + "\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\""
                            + message + "\"}"
            );
            response.getWriter().flush();
        }
    }
}
