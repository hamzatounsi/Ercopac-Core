package com.ercopac.ercopac_tracker.platform_dashboard.api;

import com.ercopac.ercopac_tracker.platform_dashboard.dto.CreateOrganisationWithAdminRequest;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.CreateOrganisationWithAdminResponse;
import com.ercopac.ercopac_tracker.platform_dashboard.service.PlatformOrganisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform/organisations")
public class PlatformOrganisationController {

    private final PlatformOrganisationService service;

    public PlatformOrganisationController(PlatformOrganisationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreateOrganisationWithAdminResponse> createOrganisationWithAdmin(
            @RequestBody CreateOrganisationWithAdminRequest request
    ) {
        return ResponseEntity.ok(service.createOrganisationWithAdmin(request));
    }

    @GetMapping
    public ResponseEntity<?> getAllOrganisations() {
        return ResponseEntity.ok(service.getAllOrganisations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrganisation(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOrganisation(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrganisation(
            @PathVariable Long id,
            @RequestBody CreateOrganisationWithAdminRequest request
    ) {
        return ResponseEntity.ok(service.updateOrganisation(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }
}