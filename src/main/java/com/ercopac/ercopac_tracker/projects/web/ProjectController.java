package com.ercopac.ercopac_tracker.projects.web;

import com.ercopac.ercopac_tracker.projects.dto.ProjectDetailsResponse;
import com.ercopac.ercopac_tracker.projects.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public ResponseEntity<ProjectDetailsResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectDetailsById(id));
    }
}