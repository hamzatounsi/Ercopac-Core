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
@PreAuthorize("hasAuthority('PLATFORM_OWNER') or hasAuthority('ROLE_PLATFORM_OWNER')")
public class PlatformDashboardController {

    private final PlatformDashboardService platformDashboardService;

    public PlatformDashboardController(PlatformDashboardService platformDashboardService) {
        this.platformDashboardService = platformDashboardService;
    }

    @GetMapping("/kpis")
    public PlatformKpiDto getKpis() {
        return platformDashboardService.getPlatformKpis();
    }

    @GetMapping("/organisations")
    public List<OrganisationSummaryDto> getOrganisations() {
        return platformDashboardService.getOrganisationSummaries();
    }

    @GetMapping("/projects")
    public List<PlatformProjectRowDto> getProjects() {
        return platformDashboardService.getGlobalProjects();
    }

    @GetMapping("/alerts")
    public List<PlatformAlertDto> getAlerts() {
        return platformDashboardService.getPlatformAlerts();
    }
}