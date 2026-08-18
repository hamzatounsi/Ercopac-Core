package com.ercopac.ercopac_tracker.projects.service;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.domain.ProjectApplicationType;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/** Centralises organisation and Project Manager ownership checks for Projectum project access. */
@Service
public class ProjectAccessService {
    private final ProjectRepository projectRepository;
    private final SecurityUtils securityUtils;

    public ProjectAccessService(ProjectRepository projectRepository, SecurityUtils securityUtils) {
        this.projectRepository = projectRepository;
        this.securityUtils = securityUtils;
    }

    public List<Project> getAccessibleProjects(ProjectApplicationType applicationType) {
        if (securityUtils.isPlatformUser()) {
            return projectRepository.findAll().stream()
                    .filter(project -> applicationType == null || project.getApplicationType() == applicationType)
                    .toList();
        }
        Long organisationId = requireOrganisationId();
        if (securityUtils.hasAnyRole("PROJECT_MANAGER_LEAD")) {
            return applicationType == null
                    ? projectRepository.findAllByOrganisationId(organisationId)
                    : projectRepository.findAllByOrganisationIdAndApplicationType(organisationId, applicationType);
        }
        if (securityUtils.hasAnyRole("PROJECT_MANAGER")) {
            return applicationType == null
                    ? projectRepository.findAllByOrganisationIdAndProjectManagerId(organisationId, securityUtils.getCurrentUserId())
                    : projectRepository.findAllByOrganisationIdAndProjectManagerIdAndApplicationType(
                            organisationId, securityUtils.getCurrentUserId(), applicationType);
        }
        return applicationType == null
                ? projectRepository.findAllByOrganisationId(organisationId)
                : projectRepository.findAllByOrganisationIdAndApplicationType(organisationId, applicationType);
    }

    public Project getAccessibleProject(Long projectId) {
        if (securityUtils.isPlatformUser()) {
            return projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        }
        Long organisationId = requireOrganisationId();
        return (securityUtils.hasAnyRole("PROJECT_MANAGER")
                ? projectRepository.findByIdAndOrganisationIdAndProjectManagerId(
                        projectId, organisationId, securityUtils.getCurrentUserId())
                : projectRepository.findByIdAndOrganisationId(projectId, organisationId))
                .orElseThrow(() -> new IllegalArgumentException("Project not accessible"));
    }

    private Long requireOrganisationId() {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        if (organisationId == null) throw new IllegalStateException("No organisation context found for current user.");
        return organisationId;
    }
}
