package com.ercopac.ercopac_tracker.tasks.web;

import com.ercopac.ercopac_tracker.tasks.dto.TaskResourceAssignmentDto;
import com.ercopac.ercopac_tracker.tasks.service.TaskResourceAssignmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/resources")
public class TaskResourceAssignmentController {

    private final TaskResourceAssignmentService service;

    public TaskResourceAssignmentController(TaskResourceAssignmentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public List<TaskResourceAssignmentDto> getTaskResources(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        return service.getTaskResources(projectId, taskId);
    }

    @PostMapping
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public TaskResourceAssignmentDto createTaskResource(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody TaskResourceAssignmentDto dto
    ) {
        return service.createTaskResource(projectId, taskId, dto);
    }

    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public TaskResourceAssignmentDto updateTaskResource(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long assignmentId,
            @RequestBody TaskResourceAssignmentDto dto
    ) {
        return service.updateTaskResource(projectId, taskId, assignmentId, dto);
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public void deleteTaskResource(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long assignmentId
    ) {
        service.deleteTaskResource(projectId, taskId, assignmentId);
    }
}