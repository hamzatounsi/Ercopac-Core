package com.ercopac.ercopac_tracker.platform_dashboard.api;

import com.ercopac.ercopac_tracker.platform_dashboard.dto.OrganisationSummaryDto;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.PlatformAlertDto;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.PlatformKpiDto;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.PlatformProjectRowDto;
import com.ercopac.ercopac_tracker.platform_dashboard.service.PlatformDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platform/dashboard")
@CrossOrigin
public class PlatformDashboardController {

    private final PlatformDashboardService platformDashboardService;

    public PlatformDashboardController(PlatformDashboardService platformDashboardService) {
        this.platformDashboardService = platformDashboardService;
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public PlatformKpiDto getKpis() {
        return platformDashboardService.getPlatformKpis();
    }

    @GetMapping("/organisations")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public List<OrganisationSummaryDto> getOrganisations() {
        return platformDashboardService.getOrganisationSummaries();
    }

    @GetMapping("/projects")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public List<PlatformProjectRowDto> getProjects() {
        return platformDashboardService.getGlobalProjects();
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public List<PlatformAlertDto> getAlerts() {
        return platformDashboardService.getPlatformAlerts();
    }
}