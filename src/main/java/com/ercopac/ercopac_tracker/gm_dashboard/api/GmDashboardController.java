package com.ercopac.ercopac_tracker.gm_dashboard.api;

import com.ercopac.ercopac_tracker.gm_dashboard.dto.PortfolioKpiDto;
import com.ercopac.ercopac_tracker.gm_dashboard.dto.ProjectDashboardRowDto;
import com.ercopac.ercopac_tracker.gm_dashboard.dto.ProjectKpiDto;
import com.ercopac.ercopac_tracker.gm_dashboard.service.GmDashboardService;
import com.ercopac.ercopac_tracker.gm_dashboard.service.GmProjectumKpiService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gm/dashboard")
public class GmDashboardController {

    private final GmDashboardService gmDashboardService;
    private final GmProjectumKpiService gmProjectumKpiService;

    public GmDashboardController(
            GmDashboardService gmDashboardService,
            GmProjectumKpiService gmProjectumKpiService
    ) {
        this.gmDashboardService = gmDashboardService;
        this.gmProjectumKpiService = gmProjectumKpiService;
    }

    @GetMapping("/projects")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public List<ProjectDashboardRowDto> projects() {
        return gmDashboardService.getProjects();
    }

    @GetMapping("/kpis/portfolio")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public PortfolioKpiDto portfolioKpis() {
        return gmProjectumKpiService.getPortfolioKpis();
    }

    @GetMapping("/projects/{projectId}/kpis")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ProjectKpiDto projectKpis(@PathVariable Long projectId) {
        return gmProjectumKpiService.getProjectKpis(projectId);
    }
}