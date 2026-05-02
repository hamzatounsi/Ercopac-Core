package com.ercopac.ercopac_tracker.user.web;

import com.ercopac.ercopac_tracker.user.dto.CreateResourceRequest;
import com.ercopac.ercopac_tracker.user.dto.ResourceDetailsDto;
import com.ercopac.ercopac_tracker.user.dto.ResourceListItemDto;
import com.ercopac.ercopac_tracker.user.dto.ResourceOptionDto;
import com.ercopac.ercopac_tracker.user.dto.UpdateResourceRequest;
import com.ercopac.ercopac_tracker.user.service.ResourceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@PreAuthorize(
    "hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PMO','PLATFORM_OWNER','PLATFORM_ADMIN')"
)
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<Page<ResourceListItemDto>> getResources(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String departmentCode,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean internalUser,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                resourceService.getResources(search, departmentCode, role, active, internalUser, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDetailsDto> getResourceById(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    @PostMapping
    public ResponseEntity<ResourceDetailsDto> createResource(@RequestBody CreateResourceRequest request) {
        return ResponseEntity.ok(resourceService.createResource(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceDetailsDto> updateResource(
            @PathVariable Long id,
            @RequestBody UpdateResourceRequest request
    ) {
        return ResponseEntity.ok(resourceService.updateResource(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateResourceStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload
    ) {
        boolean active = Boolean.TRUE.equals(payload.get("active"));
        resourceService.updateResourceStatus(id, active);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/options")
    public ResponseEntity<List<ResourceOptionDto>> getResourceOptions(
            @RequestParam(required = false) String departmentCode,
            @RequestParam(required = false) String role
    ) {
        return ResponseEntity.ok(resourceService.getResourceOptions(departmentCode, role));
    }

    @GetMapping("/meta/departments")
    public ResponseEntity<List<String>> getDepartmentCodes() {
        return ResponseEntity.ok(resourceService.getDepartmentCodes());
    }

    @GetMapping("/meta/resource-types")
    public ResponseEntity<List<String>> getResourceTypes() {
        return ResponseEntity.ok(resourceService.getResourceTypes());
    }

    @GetMapping("/meta/seniority-levels")
    public ResponseEntity<List<String>> getSeniorities() {
        return ResponseEntity.ok(resourceService.getSeniorities());
    }

    @GetMapping("/projects/{projectId}/options")
    public ResponseEntity<List<ResourceOptionDto>> getResourceOptionsForProject(
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(resourceService.getResourceOptionsForProject(projectId));
    }
}