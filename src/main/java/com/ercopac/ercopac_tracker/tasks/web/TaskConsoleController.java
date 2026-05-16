package com.ercopac.ercopac_tracker.tasks.web;

import com.ercopac.ercopac_tracker.tasks.dto.TaskConsoleConfigDto;
import com.ercopac.ercopac_tracker.tasks.dto.TaskConsoleLogDto;
import com.ercopac.ercopac_tracker.tasks.service.TaskConsoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gm/projects/{projectId}/tasks/{taskId}/console")
public class TaskConsoleController {

    private final TaskConsoleService service;

    public TaskConsoleController(TaskConsoleService service) {
        this.service = service;
    }

    @GetMapping
    public TaskConsoleConfigDto getConfig(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        return service.getConfig(getOrganisationId(), projectId, taskId);
    }

    @PutMapping
    public TaskConsoleConfigDto saveConfig(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody TaskConsoleConfigDto request
    ) {
        return service.saveConfig(getOrganisationId(), projectId, taskId, request);
    }

    @GetMapping("/logs")
    public List<TaskConsoleLogDto> getLogs(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        return service.getLogs(getOrganisationId(), projectId, taskId);
    }

    @DeleteMapping("/logs")
    public ResponseEntity<Void> clearLogs(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        service.clearLogs(getOrganisationId(), projectId, taskId);
        return ResponseEntity.noContent().build();
    }

    private Long getOrganisationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getDetails() instanceof Map<?, ?> details) {
            Object value = details.get("organisationId");
            return value == null ? null : Long.valueOf(value.toString());
        }

        return null;
    }
}