package com.ercopac.ercopac_tracker.platform_permissions.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.domain.RolePermission;
import com.ercopac.ercopac_tracker.platform_permissions.dto.RolePermissionDto;
import com.ercopac.ercopac_tracker.platform_permissions.dto.SaveRolePermissionRequest;
import com.ercopac.ercopac_tracker.platform_permissions.repository.RolePermissionRepository;
import com.ercopac.ercopac_tracker.user.Role;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class PlatformPermissionService {

    private static final String VIRTUAL_SCHEDULE = "SCHEDULE";

    private static final List<PermissionModule> SCHEDULE_MODULES = List.of(
            PermissionModule.GM_DASHBOARD,
            PermissionModule.PROJECTS,
            PermissionModule.PLANNING,
            PermissionModule.TASKS
    );

    private final OrganisationRepository organisationRepository;
    private final RolePermissionRepository permissionRepository;

    public PlatformPermissionService(
            OrganisationRepository organisationRepository,
            RolePermissionRepository permissionRepository
    ) {
        this.organisationRepository = organisationRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<String> getRoles() {
        return List.of(
                Role.ORG_ADMIN.name(),
                Role.GENERAL_MANAGER.name(),
                Role.DEPARTMENT_MANAGER.name(),
                Role.EMPLOYEE.name()
        );
    }

    public List<RolePermissionDto> getPermissions(Long organisationId, Role role) {
        Map<PermissionModule, RolePermission> existing =
                permissionRepository.findByOrganisation_IdAndRole(organisationId, role)
                        .stream()
                        .collect(java.util.stream.Collectors.toMap(RolePermission::getModule, p -> p));

        List<RolePermissionDto> result = new ArrayList<>();

        boolean scheduleRead = SCHEDULE_MODULES.stream()
                .allMatch(module -> existing.get(module) != null && existing.get(module).isCanRead());

        boolean scheduleWrite = SCHEDULE_MODULES.stream()
                .allMatch(module -> existing.get(module) != null && existing.get(module).isCanWrite());

        result.add(new RolePermissionDto(
                VIRTUAL_SCHEDULE,
                "Schedule",
                "Organisation Workspace",
                "📅",
                scheduleRead,
                scheduleWrite
        ));

        Arrays.stream(PermissionModule.values())
                .filter(module -> !SCHEDULE_MODULES.contains(module))
                .map(module -> {
                    RolePermission p = existing.get(module);

                    return new RolePermissionDto(
                            module.name(),
                            label(module),
                            group(module),
                            icon(module),
                            p != null && p.isCanRead(),
                            p != null && p.isCanWrite()
                    );
                })
                .forEach(result::add);

        return result;
    }

    @Transactional
    public List<RolePermissionDto> savePermissions(
            Long organisationId,
            Role role,
            SaveRolePermissionRequest request
    ) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation not found"));

        if (request.permissions == null) {
            return getPermissions(organisationId, role);
        }

        for (RolePermissionDto dto : request.permissions) {
            if (VIRTUAL_SCHEDULE.equals(dto.module)) {
                for (PermissionModule module : SCHEDULE_MODULES) {
                    saveSinglePermission(
                            organisation,
                            organisationId,
                            role,
                            module,
                            dto.canRead,
                            dto.canWrite
                    );
                }
                continue;
            }

            PermissionModule module = PermissionModule.valueOf(dto.module);

            saveSinglePermission(
                    organisation,
                    organisationId,
                    role,
                    module,
                    dto.canRead,
                    dto.canWrite
            );
        }

        return getPermissions(organisationId, role);
    }

    private void saveSinglePermission(
            Organisation organisation,
            Long organisationId,
            Role role,
            PermissionModule module,
            boolean canRead,
            boolean canWrite
    ) {
        RolePermission permission = permissionRepository
                .findByOrganisation_IdAndRoleAndModule(organisationId, role, module)
                .orElseGet(RolePermission::new);

        permission.setOrganisation(organisation);
        permission.setRole(role);
        permission.setModule(module);
        permission.setCanRead(canRead || canWrite);
        permission.setCanWrite(canWrite);

        permissionRepository.save(permission);
    }

    private String group(PermissionModule module) {
        return switch (module) {
            case OWNER_DASHBOARD, ORGANISATIONS, BILLING, PLATFORM_ANALYTICS,
                 INFRASTRUCTURE, SUPPORT, PERMISSIONS, PLATFORM_SETTINGS -> "Platform";

            case CRM, FINANCE, FORECAST, RISKS, CHANGE_REQUESTS,
                 ACTIONS, RESOURCES, SUPPLIERS -> "Organisation Workspace";

            case GM_DASHBOARD, PROJECTS, PLANNING, TASKS -> "Schedule";

            case DEPARTMENT_DASHBOARD -> "Department Workspace";

            case EMPLOYEE_DASHBOARD -> "Employee Workspace";
        };
    }

    private String label(PermissionModule module) {
        return switch (module) {
            case OWNER_DASHBOARD -> "Owner Dashboard";
            case ORGANISATIONS -> "Organisations";
            case BILLING -> "Billing & Plans";
            case PLATFORM_ANALYTICS -> "Platform Analytics";
            case INFRASTRUCTURE -> "Infrastructure";
            case SUPPORT -> "Support Tickets";
            case PERMISSIONS -> "Role Permissions";
            case PLATFORM_SETTINGS -> "Platform Settings";

            case GM_DASHBOARD -> "GM Dashboard";
            case CRM -> "CRM";
            case PROJECTS -> "Projects";
            case PLANNING -> "Planning & Baselines";
            case TASKS -> "Tasks & Schedule";
            case FINANCE -> "Finance";
            case FORECAST -> "Forecast";
            case RISKS -> "Risks";
            case CHANGE_REQUESTS -> "Change Requests";
            case ACTIONS -> "Actions";
            case RESOURCES -> "Resources";
            case SUPPLIERS -> "Suppliers";

            case DEPARTMENT_DASHBOARD -> "Department Dashboard";
            case EMPLOYEE_DASHBOARD -> "Employee Dashboard";
        };
    }

    private String icon(PermissionModule module) {
        return switch (module) {
            case OWNER_DASHBOARD -> "📊";
            case ORGANISATIONS -> "🏢";
            case BILLING -> "💳";
            case PLATFORM_ANALYTICS -> "📈";
            case INFRASTRUCTURE -> "🖥";
            case SUPPORT -> "🎯";
            case PERMISSIONS -> "🔐";
            case PLATFORM_SETTINGS -> "⚙";

            case GM_DASHBOARD -> "🧭";
            case CRM -> "🤝";
            case PROJECTS -> "📁";
            case PLANNING -> "📅";
            case TASKS -> "✅";
            case FINANCE -> "💶";
            case FORECAST -> "🔮";
            case RISKS -> "⚠";
            case CHANGE_REQUESTS -> "🔁";
            case ACTIONS -> "📌";
            case RESOURCES -> "👥";
            case SUPPLIERS -> "🚚";

            case DEPARTMENT_DASHBOARD -> "🏭";
            case EMPLOYEE_DASHBOARD -> "👤";
        };
    }
}