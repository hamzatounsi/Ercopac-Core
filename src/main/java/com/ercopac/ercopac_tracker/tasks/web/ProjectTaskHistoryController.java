package com.ercopac.ercopac_tracker.tasks.web;

import com.ercopac.ercopac_tracker.tasks.dto.ProjectTaskHistoryDto;
import com.ercopac.ercopac_tracker.tasks.service.ProjectTaskHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gm/projects/{projectId}/schedule/history")
@RequiredArgsConstructor
public class ProjectTaskHistoryController {

    private final ProjectTaskHistoryService service;

    @GetMapping
    public List<ProjectTaskHistoryDto> getProjectHistory(
            @PathVariable Long projectId
    ) {
        Long organisationId = getOrganisationIdFromSecurityContext();
        return service.getProjectHistory(organisationId, projectId);
    }

    @GetMapping("/tasks/{taskId}")
    public List<ProjectTaskHistoryDto> getTaskHistory(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        Long organisationId = getOrganisationIdFromSecurityContext();
        return service.getTaskHistory(organisationId, projectId, taskId);
    }

    private Long getOrganisationIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object details = authentication.getDetails();

        if (details instanceof Map<?, ?> detailsMap) {
            Object organisationId = detailsMap.get("organisationId");

            if (organisationId instanceof Long value) {
                return value;
            }

            if (organisationId instanceof Integer value) {
                return value.longValue();
            }

            if (organisationId instanceof String value) {
                return Long.valueOf(value);
            }
        }

        throw new IllegalStateException("organisationId not found in security context");
    }
}