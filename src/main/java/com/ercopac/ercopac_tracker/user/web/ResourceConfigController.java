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
@RequestMapping("/api/resource-config")
@PreAuthorize("hasAnyRole('ORG_ADMIN','GENERAL_MANAGER','PLATFORM_OWNER')")
public class ResourceConfigController {

    private final ResourceConfigService service;

    public ResourceConfigController(ResourceConfigService service) {
        this.service = service;
    }

    @GetMapping("/departments")
    public List<DepartmentDto> getDepartments() {
        return service.getDepartments();
    }

    @PostMapping("/departments")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public DepartmentDto createDepartment(@RequestBody SaveDepartmentRequest request) {
        return service.createDepartment(request);
    }

    @GetMapping("/resource-types")
    public List<ResourceTypeConfigDto> getResourceTypes() {
        return service.getResourceTypes();
    }

    @PostMapping("/resource-types")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResourceTypeConfigDto createResourceType(@RequestBody SaveResourceTypeRequest request) {
        return service.createResourceType(request);
    }

    @PutMapping("/resource-types/{id}")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResourceTypeConfigDto updateResourceType(
            @PathVariable Long id,
            @RequestBody SaveResourceTypeRequest request
    ) {
        return service.updateResourceType(id, request);
    }

    @DeleteMapping("/resource-types/{id}")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public void deleteResourceType(@PathVariable Long id) {
        service.deleteResourceType(id);
    }
}