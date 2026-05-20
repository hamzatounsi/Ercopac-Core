package com.ercopac.ercopac_tracker.planning.web;

import com.ercopac.ercopac_tracker.planning.dto.ApplyStandardTemplateResultDto;
import com.ercopac.ercopac_tracker.planning.dto.CreateProjectTemplateRequest;
import com.ercopac.ercopac_tracker.planning.dto.ProjectTemplateDto;
import com.ercopac.ercopac_tracker.planning.service.ProjectTemplateService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/templates")
public class ProjectTemplateController {

    private static final String PLANNING_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).PLANNING)";

    private static final String PLANNING_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).PLANNING)";

    private final ProjectTemplateService templateService;

    public ProjectTemplateController(ProjectTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    @PreAuthorize(PLANNING_READ)
    public List<ProjectTemplateDto> getTemplates(@PathVariable Long projectId) {
        return templateService.getProjectTemplates(projectId);
    }

    @PostMapping
    @PreAuthorize(PLANNING_WRITE)
    public ProjectTemplateDto createTemplate(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectTemplateRequest request
    ) {
        return templateService.createTemplate(projectId, request);
    }

    @DeleteMapping("/{templateId}")
    @PreAuthorize(PLANNING_WRITE)
    public void deleteTemplate(
            @PathVariable Long projectId,
            @PathVariable Long templateId
    ) {
        templateService.deleteTemplate(projectId, templateId);
    }

    @PostMapping("/apply-standard")
    @PreAuthorize(PLANNING_WRITE)
    public ApplyStandardTemplateResultDto applyStandardTemplate(@PathVariable Long projectId) {
        return templateService.applyStandardTemplate(projectId);
    }
}