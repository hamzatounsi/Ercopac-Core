package com.ercopac.ercopac_tracker.gm_dashboard.api;

import com.ercopac.ercopac_tracker.gm_dashboard.dto.ProjectDashboardRowDto;
import com.ercopac.ercopac_tracker.gm_dashboard.service.GmDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gm/dashboard")
public class GmDashboardController {

    private final GmDashboardService gmDashboardService;

    public GmDashboardController(GmDashboardService gmDashboardService) {
        this.gmDashboardService = gmDashboardService;
    }

    @GetMapping("/projects")
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public List<ProjectDashboardRowDto> projects() {
        return gmDashboardService.getProjects();
    }
}