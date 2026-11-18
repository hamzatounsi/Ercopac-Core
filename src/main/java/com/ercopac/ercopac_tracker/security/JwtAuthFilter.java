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
import java.util.Set;
import java.util.stream.Stream;
import java.time.Instant;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * The only API paths a CUSTOMER account (Ticketing portal) may call: Ticketing, its websocket,
     * the shared notifications bell, its own account settings and the auth endpoints. Everything
     * else is refused with 403 whatever the roles say.
     */
    static final List<String> CUSTOMER_ALLOWED_PREFIXES = List.of(
            "/api/tickets/", "/ws/tickets/", "/api/notifications/", "/api/users/me/", "/api/auth/"
    );

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

                Long organisationId = user.getOrganisation() == null
                        ? null
                        : user.getOrganisation().getId();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                user.getRoles().stream()
                                        .flatMap(role -> Stream.of(
                                                new SimpleGrantedAuthority(role.name()),
                                                new SimpleGrantedAuthority("ROLE_" + role.name())))
                                        .distinct()
                                        .toList()
                        );

                Map<String, Object> details = new HashMap<>();
                details.put("userId", user.getId());
                details.put("organisationId", organisationId);
                details.put("roles", user.getRoles().stream().map(Enum::name).sorted().toList());

                details.put("userType", user.getUserType().name());

                if (user.isCustomerUser() && !isCustomerAllowedPath(request.getServletPath())) {
                    sendForbidden(response, "Customer accounts can only use the Ticketing app.");
                    return;
                }

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
        if (user == null || !user.isActive() || user.getRoles().isEmpty()) {
            return false;
        }

        if (!user.hasRole(Role.PLATFORM_OWNER) && user.getOrganisation() == null) {
            return false;
        }

        Long tokenUserId = jwtService.extractUserId(token);
        Set<String> tokenRoles = jwtService.extractRoles(token).stream()
                .map(role -> role.replaceFirst("^ROLE_", ""))
                .collect(java.util.stream.Collectors.toSet());
        Long tokenOrganisationId = jwtService.extractOrganisationId(token);
        Long currentOrganisationId = user.getOrganisation() == null
                ? null
                : user.getOrganisation().getId();
        Set<String> currentRoles = user.getRoles().stream().map(Enum::name)
                .collect(java.util.stream.Collectors.toSet());

        if (!user.getId().equals(tokenUserId)
                || !currentRoles.equals(tokenRoles)
                || !java.util.Objects.equals(currentOrganisationId, tokenOrganisationId)) {
            return false;
        }

        return user.getOrganisation() == null
                || (user.getOrganisation().isActive()
                && user.getOrganisation().getStatus() != OrganisationStatus.SUSPENDED);
    }

    static boolean isCustomerAllowedPath(String path) {
        if (path == null) {
            return false;
        }
        String normalised = path.endsWith("/") ? path : path + "/";
        return CUSTOMER_ALLOWED_PREFIXES.stream().anyMatch(normalised::startsWith);
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();

        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"timestamp\":\"" + Instant.now()
                            + "\",\"status\":403,\"error\":\"Forbidden\",\"code\":\"FORBIDDEN\",\"message\":\""
                            + message + "\"}"
            );
            response.getWriter().flush();
        }
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
