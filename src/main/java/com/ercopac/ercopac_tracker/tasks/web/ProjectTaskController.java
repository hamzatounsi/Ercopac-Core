package com.ercopac.ercopac_tracker.tasks.web;

import com.ercopac.ercopac_tracker.tasks.dto.ProjectScheduleTaskResponse;
import com.ercopac.ercopac_tracker.tasks.dto.UpdateProjectTaskRequest;
import com.ercopac.ercopac_tracker.tasks.service.ProjectTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class ProjectTaskController {

    private final ProjectTaskService projectTaskService;

    public ProjectTaskController(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public ResponseEntity<ProjectScheduleTaskResponse> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateProjectTaskRequest request) {
        return ResponseEntity.ok(projectTaskService.updateTask(taskId, request));
    }
}