package com.ercopac.ercopac_tracker.tasks.web;

import com.ercopac.ercopac_tracker.tasks.dto.CreateTaskRequest;
import com.ercopac.ercopac_tracker.tasks.dto.ProjectScheduleTaskResponse;
import com.ercopac.ercopac_tracker.tasks.dto.ResourceUserDto;
import com.ercopac.ercopac_tracker.tasks.dto.UpdateProjectTaskRequest;
import com.ercopac.ercopac_tracker.tasks.service.ProjectTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@PreAuthorize("hasAnyAuthority('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
public class ProjectTaskController {

    private final ProjectTaskService projectTaskService;

    public ProjectTaskController(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @PutMapping("/api/projects/{projectId}/tasks/{taskId}")
    public ResponseEntity<ProjectScheduleTaskResponse> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody UpdateProjectTaskRequest request) {
        return ResponseEntity.ok(projectTaskService.updateTask(taskId, request));
    }

    @PostMapping("/api/projects/{projectId}/tasks/below/{afterTaskId}")
    public ResponseEntity<ProjectScheduleTaskResponse> createTaskBelow(
            @PathVariable Long projectId,
            @PathVariable Long afterTaskId,
            @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(projectTaskService.createTaskBelow(projectId, afterTaskId, request));
    }

    @PostMapping("/api/projects/{projectId}/tasks/copy/{taskId}")
    public ResponseEntity<ProjectScheduleTaskResponse> copyTaskBelow(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        System.out.println("%%% COPY ENDPOINT HIT project=" + projectId + " task=" + taskId);
        return ResponseEntity.ok(projectTaskService.copyTaskBelow(projectId, taskId));
    }

    @DeleteMapping("/api/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        projectTaskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/projects/{projectId}/tasks/resource-users")
    public ResponseEntity<List<ResourceUserDto>> getResourceUsers(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(projectTaskService.getResourceUsersForProject(projectId));
    }
}