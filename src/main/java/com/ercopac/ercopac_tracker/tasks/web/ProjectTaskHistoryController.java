package com.ercopac.ercopac_tracker.tasks.web;

import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.dto.ProjectTaskHistoryDto;
import com.ercopac.ercopac_tracker.tasks.service.ProjectTaskHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gm/projects/{projectId}/schedule/history")
@RequiredArgsConstructor
public class ProjectTaskHistoryController {

    private static final String TASKS_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).TASKS)";

    private final ProjectTaskHistoryService service;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize(TASKS_READ)
    public List<ProjectTaskHistoryDto> getProjectHistory(@PathVariable Long projectId) {
        return service.getProjectHistory(securityUtils.getCurrentOrganisationId(), projectId);
    }

    @GetMapping("/tasks/{taskId}")
    @PreAuthorize(TASKS_READ)
    public List<ProjectTaskHistoryDto> getTaskHistory(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        return service.getTaskHistory(securityUtils.getCurrentOrganisationId(), projectId, taskId);
    }
}