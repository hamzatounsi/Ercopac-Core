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

        if (user.getRole() == Role.PLATFORM_OWNER) {
            return true;
        }

        if (user.getOrganisation() == null) {
            return false;
        }

        if (module == PermissionModule.CRM && user.getRole().isCrmRole()) {
            return true;
        }

        Role effectiveRole = user.getRole() == Role.PROJECT_MANAGER_LEAD ? Role.PROJECT_MANAGER : user.getRole();
        return permissionRepository
                .findByOrganisation_IdAndRoleAndModule(
                        user.getOrganisation().getId(),
                        effectiveRole,
                        module
                )
                .map(RolePermission::isCanRead)
                .orElse(false);
    }

    public boolean canWrite(Authentication authentication, PermissionModule module) {
        AppUser user = getCurrentUser(authentication);

        if (user.getRole() == Role.PLATFORM_OWNER) {
            return true;
        }

        if (user.getOrganisation() == null) {
            return false;
        }

        if (module == PermissionModule.CRM) {
            if (user.getRole().isSalesManagerRole()) {
                return true;
            }
        }

        Role effectiveRole = user.getRole() == Role.PROJECT_MANAGER_LEAD ? Role.PROJECT_MANAGER : user.getRole();
        return permissionRepository
                .findByOrganisation_IdAndRoleAndModule(
                        user.getOrganisation().getId(),
                        effectiveRole,
                        module
                )
                .map(RolePermission::isCanWrite)
                .orElse(false);
    }

    public boolean canAccessCrmManagerView(Authentication authentication) {
        return getCurrentUser(authentication).getRole() == Role.SALES_MANAGER_LEAD;
    }

    private AppUser getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        return userRepository.findByEmail1(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
