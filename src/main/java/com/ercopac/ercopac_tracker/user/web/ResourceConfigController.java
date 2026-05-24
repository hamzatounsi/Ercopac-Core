package com.ercopac.ercopac_tracker.user.web;

import com.ercopac.ercopac_tracker.department.dto.DepartmentDto;
import com.ercopac.ercopac_tracker.department.dto.SaveDepartmentRequest;
import com.ercopac.ercopac_tracker.user.dto.ResourceTypeConfigDto;
import com.ercopac.ercopac_tracker.user.dto.SaveResourceTypeRequest;
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

    private final ResourceConfigService service;

    public ResourceConfigController(ResourceConfigService service) {
        this.service = service;
    }

    @GetMapping("/departments")
    @PreAuthorize(RESOURCES_READ)
    public List<DepartmentDto> getDepartments() {
        return service.getDepartments();
    }

    @PostMapping("/departments")
    @PreAuthorize(RESOURCES_WRITE)
    public DepartmentDto createDepartment(@RequestBody SaveDepartmentRequest request) {
        return service.createDepartment(request);
    }

    @GetMapping("/resource-types")
    @PreAuthorize(RESOURCES_READ)
    public List<ResourceTypeConfigDto> getResourceTypes() {
        return service.getResourceTypes();
    }

    @PostMapping("/resource-types")
    @PreAuthorize(RESOURCES_WRITE)
    public ResourceTypeConfigDto createResourceType(@RequestBody SaveResourceTypeRequest request) {
        return service.createResourceType(request);
    }

    @PutMapping("/resource-types/{id}")
    @PreAuthorize(RESOURCES_WRITE)
    public ResourceTypeConfigDto updateResourceType(
            @PathVariable Long id,
            @RequestBody SaveResourceTypeRequest request
    ) {
        return service.updateResourceType(id, request);
    }

    @DeleteMapping("/resource-types/{id}")
    @PreAuthorize(RESOURCES_WRITE)
    public void deleteResourceType(@PathVariable Long id) {
        service.deleteResourceType(id);
    }
}