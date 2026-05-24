package com.ercopac.ercopac_tracker.projects.web;

import com.ercopac.ercopac_tracker.gm_dashboard.dto.ProjectDashboardRowDto;
import com.ercopac.ercopac_tracker.projects.dto.ProjectDetailsResponse;
import com.ercopac.ercopac_tracker.projects.dto.UpsertProjectRequest;
import com.ercopac.ercopac_tracker.projects.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final String PROJECTS_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).PROJECTS)";

    private static final String PROJECTS_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).PROJECTS)";

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/{id}")
    @PreAuthorize(PROJECTS_READ)
    public ResponseEntity<ProjectDetailsResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectDetailsById(id));
    }

    @PostMapping
    @PreAuthorize(PROJECTS_WRITE)
    public ResponseEntity<ProjectDashboardRowDto> createProject(
            @RequestBody UpsertProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize(PROJECTS_WRITE)
    public ResponseEntity<ProjectDashboardRowDto> updateProject(
            @PathVariable Long id,
            @RequestBody UpsertProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize(PROJECTS_WRITE)
    public ResponseEntity<Void> archiveProject(@PathVariable Long id) {
        projectService.archiveProject(id);
        return ResponseEntity.ok().build();
    }
}