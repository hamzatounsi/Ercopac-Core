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
public class ProjectTaskController {

    private static final String TASKS_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).TASKS)";

    private static final String TASKS_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).TASKS)";

    private final ProjectTaskService projectTaskService;

    public ProjectTaskController(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @PutMapping("/api/projects/{projectId}/tasks/{taskId}")
    @PreAuthorize(TASKS_WRITE)
    public ResponseEntity<ProjectScheduleTaskResponse> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody UpdateProjectTaskRequest request) {
        return ResponseEntity.ok(projectTaskService.updateTask(taskId, request));
    }

    @PostMapping("/api/projects/{projectId}/tasks/below/{afterTaskId}")
    @PreAuthorize(TASKS_WRITE)
    public ResponseEntity<ProjectScheduleTaskResponse> createTaskBelow(
            @PathVariable Long projectId,
            @PathVariable Long afterTaskId,
            @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(projectTaskService.createTaskBelow(projectId, afterTaskId, request));
    }

    @PostMapping("/api/projects/{projectId}/tasks/copy/{taskId}")
    @PreAuthorize(TASKS_WRITE)
    public ResponseEntity<ProjectScheduleTaskResponse> copyTaskBelow(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(projectTaskService.copyTaskBelow(projectId, taskId));
    }

    @DeleteMapping("/api/tasks/{taskId}")
    @PreAuthorize(TASKS_WRITE)
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        projectTaskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/projects/{projectId}/tasks/resource-users")
    @PreAuthorize(TASKS_READ)
    public ResponseEntity<List<ResourceUserDto>> getResourceUsers(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectTaskService.getResourceUsersForProject(projectId));
    }
}