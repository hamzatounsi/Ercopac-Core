package com.ercopac.ercopac_tracker.platform_permissions.repository;

import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.domain.RolePermission;
import com.ercopac.ercopac_tracker.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByOrganisation_IdAndRole(Long organisationId, Role role);

    Optional<RolePermission> findByOrganisation_IdAndRoleAndModule(
            Long organisationId,
            Role role,
            PermissionModule module
    );
}