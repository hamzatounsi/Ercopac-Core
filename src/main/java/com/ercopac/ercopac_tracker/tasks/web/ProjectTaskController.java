package com.ercopac.ercopac_tracker.tasks.web;

import com.ercopac.ercopac_tracker.tasks.dto.CreateTaskRequest;
import com.ercopac.ercopac_tracker.tasks.dto.ProjectScheduleTaskResponse;
import com.ercopac.ercopac_tracker.tasks.dto.UpdateProjectTaskRequest;
import com.ercopac.ercopac_tracker.tasks.service.ProjectTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProjectTaskController {

    private final ProjectTaskService projectTaskService;

    public ProjectTaskController(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @PutMapping("/api/tasks/{taskId}")
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public ResponseEntity<ProjectScheduleTaskResponse> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateProjectTaskRequest request) {
        return ResponseEntity.ok(projectTaskService.updateTask(taskId, request));
    }

    @PostMapping("/api/projects/{projectId}/tasks/below/{afterTaskId}")
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public ResponseEntity<ProjectScheduleTaskResponse> createTaskBelow(
            @PathVariable Long projectId,
            @PathVariable Long afterTaskId,
            @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(projectTaskService.createTaskBelow(projectId, afterTaskId, request));
    }

    @PostMapping("/api/projects/{projectId}/tasks/copy/{taskId}")
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public ResponseEntity<ProjectScheduleTaskResponse> copyTaskBelow(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(projectTaskService.copyTaskBelow(projectId, taskId));
    }

    @DeleteMapping("/api/tasks/{taskId}")
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        projectTaskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}