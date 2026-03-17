package com.ercopac.ercopac_tracker.tasks.web;

import com.ercopac.ercopac_tracker.tasks.dto.ProjectScheduleTaskResponse;
import com.ercopac.ercopac_tracker.tasks.service.ProjectScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectScheduleController {

    private final ProjectScheduleService projectScheduleService;

    public ProjectScheduleController(ProjectScheduleService projectScheduleService) {
        this.projectScheduleService = projectScheduleService;
    }

    @GetMapping("/{projectId}/schedule")
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public ResponseEntity<List<ProjectScheduleTaskResponse>> getProjectSchedule(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectScheduleService.getProjectSchedule(projectId));
    }
}