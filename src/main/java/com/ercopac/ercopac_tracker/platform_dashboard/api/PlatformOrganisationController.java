package com.ercopac.ercopac_tracker.platform_dashboard.api;

import com.ercopac.ercopac_tracker.platform_dashboard.dto.CreateOrganisationWithAdminRequest;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.CreateOrganisationWithAdminResponse;
import com.ercopac.ercopac_tracker.platform_dashboard.service.PlatformOrganisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform/organisations")
@PreAuthorize("hasAnyRole('PLATFORM_OWNER','PLATFORM_ADMIN')")
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
}