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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    public String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            return null;
        }
        String role = auth.getAuthorities().iterator().next().getAuthority();
        System.out.println("CURRENT ROLE = " + role);
        return role;
    }

    public Long getCurrentOrganisationId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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
        String role = getCurrentRole();
        return "ROLE_PLATFORM_OWNER".equals(role)
                || "ROLE_PLATFORM_ADMIN".equals(role)
                || "ROLE_OWNER".equals(role);
    }

    public boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }

        Set<String> authorities = auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        for (String role : roles) {
            if (authorities.contains(role) || authorities.contains("ROLE_" + role)) {
                return true;
            }
        }

        return false;
    }
}