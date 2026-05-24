package com.ercopac.ercopac_tracker.platform_permissions.api;

import com.ercopac.ercopac_tracker.platform_permissions.dto.SaveRolePermissionRequest;
import com.ercopac.ercopac_tracker.platform_permissions.service.PlatformPermissionService;
import com.ercopac.ercopac_tracker.user.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform/permissions")
public class PlatformPermissionController {

    private final PlatformPermissionService service;

    public PlatformPermissionController(PlatformPermissionService service) {
        this.service = service;
    }

    @GetMapping("/roles")
    public ResponseEntity<?> roles() {
        return ResponseEntity.ok(service.getRoles());
    }

    @GetMapping("/{organisationId}/{role}")
    public ResponseEntity<?> getPermissions(
            @PathVariable Long organisationId,
            @PathVariable Role role
    ) {
        return ResponseEntity.ok(service.getPermissions(organisationId, role));
    }

    @PutMapping("/{organisationId}/{role}")
    public ResponseEntity<?> savePermissions(
            @PathVariable Long organisationId,
            @PathVariable Role role,
            @RequestBody SaveRolePermissionRequest request
    ) {
        return ResponseEntity.ok(service.savePermissions(organisationId, role, request));
    }
}