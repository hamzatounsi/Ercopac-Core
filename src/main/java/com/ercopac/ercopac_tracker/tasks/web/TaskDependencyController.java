package com.ercopac.ercopac_tracker.tasks.web;

import com.ercopac.ercopac_tracker.tasks.dto.TaskDependencyDto;
import com.ercopac.ercopac_tracker.tasks.service.TaskDependencyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/dependencies")
public class TaskDependencyController {

    private static final String TASKS_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).TASKS)";

    private static final String TASKS_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).TASKS)";

    private final TaskDependencyService service;

    public TaskDependencyController(TaskDependencyService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize(TASKS_READ)
    public List<TaskDependencyDto> getDependencies(@PathVariable Long projectId) {
        return service.getProjectDependencies(projectId);
    }

    @PostMapping
    @PreAuthorize(TASKS_WRITE)
    public TaskDependencyDto createDependency(
            @PathVariable Long projectId,
            @RequestBody TaskDependencyDto dto
    ) {
        return service.createDependency(dto, projectId);
    }

    @PutMapping("/{dependencyId}")
    @PreAuthorize(TASKS_WRITE)
    public TaskDependencyDto updateDependency(
            @PathVariable Long projectId,
            @PathVariable Long dependencyId,
            @RequestBody TaskDependencyDto dto
    ) {
        return service.updateDependency(dependencyId, dto, projectId);
    }

    @DeleteMapping("/{dependencyId}")
    @PreAuthorize(TASKS_WRITE)
    public void deleteDependency(
            @PathVariable Long projectId,
            @PathVariable Long dependencyId
    ) {
        service.deleteDependency(dependencyId, projectId);
    }
}