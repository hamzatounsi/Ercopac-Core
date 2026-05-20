package com.ercopac.ercopac_tracker.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SecurityUtils {

    public String getCurrentUsername() {
        Authentication auth = currentAuthentication();
        return auth != null ? auth.getName() : null;
    }

    public String getCurrentRole() {
        Authentication auth = currentAuthentication();

        if (auth == null || auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            return null;
        }

        return auth.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");
    }

    public Long getCurrentOrganisationId() {
        Authentication auth = currentAuthentication();

        if (auth == null || auth.getDetails() == null) {
            throw new IllegalStateException("No authenticated user organisation found");
        }

        if (auth.getDetails() instanceof Map<?, ?> details) {
            Object value = details.get("organisationId");

            if (value == null) {
                throw new IllegalStateException("Authenticated user has no organisationId");
            }

            return Long.valueOf(value.toString());
        }

        throw new IllegalStateException("Authentication details do not contain organisationId");
    }

    public Long getCurrentUserId() {
        Authentication auth = currentAuthentication();

        if (auth == null || auth.getDetails() == null) {
            throw new IllegalStateException("No authenticated user id found");
        }

        if (auth.getDetails() instanceof Map<?, ?> details) {
            Object value = details.get("userId");

            if (value == null) {
                throw new IllegalStateException("Authenticated user has no userId");
            }

            return Long.valueOf(value.toString());
        }

        throw new IllegalStateException("Authentication details do not contain userId");
    }

    public boolean isPlatformUser() {
        return hasAnyRole("PLATFORM_OWNER");
    }

    public boolean hasAnyRole(String... roles) {
        Authentication auth = currentAuthentication();

        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }

        Set<String> authorities = auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        for (String role : roles) {
            String cleanRole = role.replace("ROLE_", "");

            if (
                    authorities.contains(cleanRole) ||
                    authorities.contains("ROLE_" + cleanRole)
            ) {
                return true;
            }
        }

        return false;
    }

    public boolean isAuthenticated() {
        Authentication auth = currentAuthentication();
        return auth != null && auth.isAuthenticated();
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}