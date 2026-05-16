package com.ercopac.ercopac_tracker.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ApplicationContext applicationContext;

    public JwtAuthFilter(JwtService jwtService, ApplicationContext applicationContext) {
        this.jwtService = jwtService;
        this.applicationContext = applicationContext;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/api/auth/")
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
                UserDetailsService userDetailsService =
                        applicationContext.getBean(UserDetailsService.class);

                UserDetails user = userDetailsService.loadUserByUsername(username);

                String role = jwtService.extractRole(token);

                if (role == null || role.isBlank()) {
                    sendUnauthorized(response, "JWT role is missing");
                    return;
                }

                String cleanRole = role.startsWith("ROLE_")
                        ? role.substring(5)
                        : role;

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(cleanRole),
                                        new SimpleGrantedAuthority("ROLE_" + cleanRole)
                                )
                        );

                Map<String, Object> details = new HashMap<>();
                details.put("userId", jwtService.extractUserId(token));
                details.put("organisationId", jwtService.extractOrganisationId(token));
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

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();

        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + message + "\"}");
            response.getWriter().flush();
        }
    }
}