package com.ercopac.ercopac_tracker.platform_permissions.service;

import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.repository.RolePermissionRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MyPermissionService {

    private final UserRepository userRepository;
    private final RolePermissionRepository permissionRepository;
    private final SecurityUtils securityUtils;

    public MyPermissionService(UserRepository userRepository, RolePermissionRepository permissionRepository, SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.securityUtils = securityUtils;
    }

    public List<Map<String, Object>> getMyPermissions() {
        String email = securityUtils.getCurrentUsername();

        AppUser user = userRepository.findByEmail1(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.PLATFORM_OWNER) {
            return Arrays.stream(PermissionModule.values())
                    .map(module -> permission(module.name(), true, true))
                    .toList();
        }

        if (user.getOrganisation() == null) {
            return List.of();
        }

        return Arrays.stream(PermissionModule.values())
                .map(module -> {
                    var saved = permissionRepository
                            .findByOrganisation_IdAndRoleAndModule(
                                    user.getOrganisation().getId(),
                                    user.getRole(),
                                    module
                            );

                    return permission(
                            module.name(),
                            saved.map(p -> p.isCanRead()).orElse(false),
                            saved.map(p -> p.isCanWrite()).orElse(false)
                    );
                })
                .toList();
    }

    private Map<String, Object> permission(String module, boolean canRead, boolean canWrite) {
        Map<String, Object> map = new HashMap<>();
        map.put("module", module);
        map.put("canRead", canRead);
        map.put("canWrite", canWrite);
        return map;
    }
}