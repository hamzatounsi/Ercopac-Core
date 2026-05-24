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

    private static final String GM_DASHBOARD_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).GM_DASHBOARD)";

    private static final String PROJECTS_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).PROJECTS)";

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
    @PreAuthorize(PROJECTS_READ)
    public List<ProjectDashboardRowDto> projects() {
        return gmDashboardService.getProjects();
    }

    @GetMapping("/kpis/portfolio")
    @PreAuthorize(GM_DASHBOARD_READ)
    public PortfolioKpiDto portfolioKpis() {
        return gmProjectumKpiService.getPortfolioKpis();
    }

    @GetMapping("/projects/{projectId}/kpis")
    @PreAuthorize(GM_DASHBOARD_READ)
    public ProjectKpiDto projectKpis(@PathVariable Long projectId) {
        return gmProjectumKpiService.getProjectKpis(projectId);
    }
}