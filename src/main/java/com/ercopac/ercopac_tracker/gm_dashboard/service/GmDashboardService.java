package com.ercopac.ercopac_tracker.gm_dashboard.service;

import com.ercopac.ercopac_tracker.gm_dashboard.dto.ProjectDashboardRowDto;
import com.ercopac.ercopac_tracker.kpi.domain.HealthStatus;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.domain.ProjectApplicationType;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class GmDashboardService {

    private final ProjectRepository projectRepository;
    private final SecurityUtils securityUtils;

    public GmDashboardService(ProjectRepository projectRepository, SecurityUtils securityUtils) {
        this.projectRepository = projectRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<ProjectDashboardRowDto> getProjects(String applicationType) {
        LocalDate today = LocalDate.now();
        ProjectApplicationType type = ProjectApplicationType.valueOf(applicationType.toUpperCase());

        return getAccessibleProjects(type)
                .stream()
                .map(p -> {
                    ProjectDashboardRowDto dto = new ProjectDashboardRowDto();
                    dto.setId(p.getId());
                    dto.setCode(p.getCode());
                    dto.setName(p.getName());
                    dto.setShortName(p.getShortName());
                    dto.setCustomer(p.getCustomer());
                    dto.setCategory(p.getCategory());
                    dto.setCountry(p.getCountry());
                    dto.setPortfolio(p.getPortfolio());
                    dto.setProjectType(p.getProjectType());
                    dto.setProjectPhase(p.getProjectPhase());
                    dto.setRiskLevel(p.getRiskLevel());
                    dto.setProjectManagerName(p.getProjectManagerName());
                    dto.setProgramManagerName(p.getProgramManagerName());
                    dto.setSalesManagerName(p.getSalesManagerName());
                    dto.setPlannedStart(p.getPlannedStart());
                    dto.setPlannedEnd(p.getPlannedEnd());
                    dto.setProjectBudget(p.getProjectBudget());
                    dto.setEstimatedCost(p.getEstimatedCost());
                    // Progress is a persisted project value. Do not derive it from
                    // health colours or incomplete task data in the client.
                    dto.setProgressPercent(p.getProgress());
                    dto.setArchived(p.getArchived());
                    dto.setTimeHealth(computeTimeHealth(p.getPlannedEnd(), today).name());
                    dto.setApplicationType(
                        p.getApplicationType() != null ? p.getApplicationType().name() : "PROJECTUM"
                    );
                    return dto;
                })
                .toList();
    }
    private List<Project> getAccessibleProjects(ProjectApplicationType applicationType) {
        if (securityUtils.isPlatformUser()) {
            return projectRepository.findAll()
                    .stream()
                    .filter(p -> p.getApplicationType() == applicationType)
                    .toList();
        }

        Long organisationId = securityUtils.getCurrentOrganisationId();
        if (organisationId == null) {
            throw new IllegalStateException("No organisation context found for current user.");
        }

        return projectRepository.findAllByOrganisationIdAndApplicationType(
                organisationId,
                applicationType
        );
    }

    private HealthStatus computeTimeHealth(LocalDate plannedEnd, LocalDate today) {
        if (plannedEnd == null) {
            return HealthStatus.NA;
        }

        if (today.isAfter(plannedEnd)) {
            return HealthStatus.RED;
        }

        if (!today.isBefore(plannedEnd.minusDays(7))) {
            return HealthStatus.YELLOW;
        }

        return HealthStatus.GREEN;
    }
}
