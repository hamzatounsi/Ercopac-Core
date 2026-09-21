package com.ercopac.ercopac_tracker.platform_permissions.security;

import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.domain.RolePermission;
import com.ercopac.ercopac_tracker.platform_permissions.repository.RolePermissionRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("permissionChecker")
public class PermissionChecker {

    private final RolePermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public PermissionChecker(
            RolePermissionRepository permissionRepository,
            UserRepository userRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    public boolean canRead(Authentication authentication, PermissionModule module) {
        AppUser user = getCurrentUser(authentication);
        if (user.hasRole(Role.PLATFORM_OWNER)) {
            return true;
        }
        if (user.getOrganisation() == null) {
            return false;
        }
        if (module == PermissionModule.CRM && user.getRoles().stream().anyMatch(Role::isCrmRole)) {
            return true;
        }
        // ✅ FIX: le rôle MANAGER (Command Center) doit pouvoir lire les
        // Tasks/Milestones sans dépendre d'une ligne role_permissions
        // configurée manuellement en base — cohérent avec son accès déjà
        // accordé (via @PreAuthorize statique) aux autres endpoints
        // Command Center comme /company-dashboard et /revenue-forecast.
        if (module == PermissionModule.TASKS && user.hasRole(Role.MANAGER)) {
            return true;
        }
        
        return user.getRoles().stream().map(this::effectiveRole).distinct().anyMatch(role ->
                permissionRepository.findByOrganisation_IdAndRoleAndModule(
                        user.getOrganisation().getId(), role, module)
                        .map(RolePermission::isCanRead).orElse(false));
    }

    public boolean canWrite(Authentication authentication, PermissionModule module) {
        AppUser user = getCurrentUser(authentication);
        if (user.hasRole(Role.PLATFORM_OWNER)) {
            return true;
        }
        if (user.getOrganisation() == null) {
            return false;
        }
        if (module == PermissionModule.CRM) {
            if (user.getRoles().stream().anyMatch(Role::isSalesManagerRole)) {
                return true;
            }
        }
        return user.getRoles().stream().map(this::effectiveRole).distinct().anyMatch(role ->
                permissionRepository.findByOrganisation_IdAndRoleAndModule(
                        user.getOrganisation().getId(), role, module)
                        .map(RolePermission::isCanWrite).orElse(false));
    }

    public boolean canAccessCrmManagerView(Authentication authentication) {
        return getCurrentUser(authentication).hasRole(Role.SALES_MANAGER_LEAD);
    }

    private Role effectiveRole(Role role) {
        return role == Role.PROJECT_MANAGER_LEAD ? Role.PROJECT_MANAGER : role;
    }

    private AppUser getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail1(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
