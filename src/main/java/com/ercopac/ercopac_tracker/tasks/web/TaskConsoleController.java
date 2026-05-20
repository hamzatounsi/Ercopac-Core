package com.ercopac.ercopac_tracker.tasks.web;

import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.dto.TaskConsoleConfigDto;
import com.ercopac.ercopac_tracker.tasks.dto.TaskConsoleLogDto;
import com.ercopac.ercopac_tracker.tasks.service.TaskConsoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gm/projects/{projectId}/tasks/{taskId}/console")
public class TaskConsoleController {

    private static final String TASKS_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).TASKS)";

    private static final String TASKS_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).TASKS)";

    private final TaskConsoleService service;
    private final SecurityUtils securityUtils;

    public TaskConsoleController(TaskConsoleService service, SecurityUtils securityUtils) {
        this.service = service;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    @PreAuthorize(TASKS_READ)
    public TaskConsoleConfigDto getConfig(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        return service.getConfig(securityUtils.getCurrentOrganisationId(), projectId, taskId);
    }

    @PutMapping
    @PreAuthorize(TASKS_WRITE)
    public TaskConsoleConfigDto saveConfig(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody TaskConsoleConfigDto request
    ) {
        return service.saveConfig(securityUtils.getCurrentOrganisationId(), projectId, taskId, request);
    }

    @GetMapping("/logs")
    @PreAuthorize(TASKS_READ)
    public List<TaskConsoleLogDto> getLogs(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        return service.getLogs(securityUtils.getCurrentOrganisationId(), projectId, taskId);
    }

    @DeleteMapping("/logs")
    @PreAuthorize(TASKS_WRITE)
    public ResponseEntity<Void> clearLogs(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        service.clearLogs(securityUtils.getCurrentOrganisationId(), projectId, taskId);
        return ResponseEntity.noContent().build();
    }
}