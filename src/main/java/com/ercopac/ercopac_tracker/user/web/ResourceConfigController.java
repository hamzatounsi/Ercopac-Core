package com.ercopac.ercopac_tracker.user.web;

import com.ercopac.ercopac_tracker.department.dto.DepartmentDto;
import com.ercopac.ercopac_tracker.department.dto.SaveDepartmentRequest;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.dto.ResourceTypeConfigDto;
import com.ercopac.ercopac_tracker.user.dto.SaveResourceTypeRequest;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.user.service.ResourceConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ResourceConfigController {

    private static final String RESOURCES_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).RESOURCES)";

    private static final String RESOURCES_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).RESOURCES)";

    private static final String ORG_ADMIN_OR_RESOURCES_READ =
            "hasAnyAuthority('ORG_ADMIN','ROLE_ORG_ADMIN') or " + RESOURCES_READ;

    private static final String ORG_ADMIN_OR_RESOURCES_WRITE =
            "hasAnyAuthority('ORG_ADMIN','ROLE_ORG_ADMIN') or " + RESOURCES_WRITE;

    private final ResourceConfigService service;
    
    // ✅ NEW: Injected dependencies for the new endpoint
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public ResourceConfigController(
            ResourceConfigService service,
            UserRepository userRepository,   // ✅ ADDED
            SecurityUtils securityUtils      // ✅ ADDED
    ) {
        this.service = service;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/departments")
    @PreAuthorize(ORG_ADMIN_OR_RESOURCES_READ)
    public List<DepartmentDto> getDepartments() {
        return service.getDepartments();
    }

    @PostMapping("/departments")
    @PreAuthorize(ORG_ADMIN_OR_RESOURCES_WRITE)
    public DepartmentDto createDepartment(@RequestBody SaveDepartmentRequest request) {
        return service.createDepartment(request);
    }

    // ✅ NEW: Endpoint to fetch users for a specific department (for the cascading dropdown)
    @GetMapping("/departments/{departmentId}/users")
    @PreAuthorize(ORG_ADMIN_OR_RESOURCES_READ)
    public List<UserSummaryDto> getUsersByDepartment(@PathVariable Long departmentId) {
        Long orgId = securityUtils.getCurrentOrganisationId();
        if (orgId == null) {
            throw new IllegalStateException("User has no organisation");
        }

        return userRepository.findByOrganisation_IdAndDepartment1_IdOrderByFullNameAsc(orgId, departmentId)
                .stream()
                .filter(AppUser::isActive)
                .map(user -> new UserSummaryDto(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getDepartment() != null ? user.getDepartment().getId() : null
                ))
                .toList();
    }

    @GetMapping("/resource-types")
    @PreAuthorize(ORG_ADMIN_OR_RESOURCES_READ)
    public List<ResourceTypeConfigDto> getResourceTypes() {
        return service.getResourceTypes();
    }

    @PostMapping("/resource-types")
    @PreAuthorize(ORG_ADMIN_OR_RESOURCES_WRITE)
    public ResourceTypeConfigDto createResourceType(@RequestBody SaveResourceTypeRequest request) {
        return service.createResourceType(request);
    }

    @PutMapping("/resource-types/{id}")
    @PreAuthorize(ORG_ADMIN_OR_RESOURCES_WRITE)
    public ResourceTypeConfigDto updateResourceType(
            @PathVariable Long id,
            @RequestBody SaveResourceTypeRequest request
    ) {
        return service.updateResourceType(id, request);
    }

    @DeleteMapping("/resource-types/{id}")
    @PreAuthorize(ORG_ADMIN_OR_RESOURCES_WRITE)
    public void deleteResourceType(@PathVariable Long id) {
        service.deleteResourceType(id);
    }

    // ─── Simple DTO for the user dropdown ────────────────────────────────────────
    public static class UserSummaryDto {
        private Long id;
        private String fullName;
        private String email;
        private Long departmentId;

        public UserSummaryDto(Long id, String fullName, String email, Long departmentId) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.departmentId = departmentId;
        }

        public Long getId() { return id; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public Long getDepartmentId() { return departmentId; }
    }
}
