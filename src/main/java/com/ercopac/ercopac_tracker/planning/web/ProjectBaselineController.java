package com.ercopac.ercopac_tracker.planning.web;

import com.ercopac.ercopac_tracker.planning.dto.CreateProjectBaselineRequest;
import com.ercopac.ercopac_tracker.planning.dto.ProjectBaselineDto;
import com.ercopac.ercopac_tracker.planning.service.ProjectBaselineService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/baselines")
public class ProjectBaselineController {

    private static final String PLANNING_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).PLANNING)";

    private static final String PLANNING_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).PLANNING)";

    private final ProjectBaselineService baselineService;

    public ProjectBaselineController(ProjectBaselineService baselineService) {
        this.baselineService = baselineService;
    }

    @GetMapping
    @PreAuthorize(PLANNING_READ)
    public List<ProjectBaselineDto> getBaselines(@PathVariable Long projectId) {
        return baselineService.getProjectBaselines(projectId);
    }

    @PostMapping
    @PreAuthorize(PLANNING_WRITE)
    public ProjectBaselineDto createBaseline(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectBaselineRequest request
    ) {
        return baselineService.createBaseline(projectId, request);
    }

    @DeleteMapping("/{baselineId}")
    @PreAuthorize(PLANNING_WRITE)
    public void deleteBaseline(
            @PathVariable Long projectId,
            @PathVariable Long baselineId
    ) {
        baselineService.deleteBaseline(projectId, baselineId);
    }
}