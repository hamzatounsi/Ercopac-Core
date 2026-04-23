package com.ercopac.ercopac_tracker.projects.service;

import com.ercopac.ercopac_tracker.gm_dashboard.dto.ProjectDashboardRowDto;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.planning.domain.ProjectPlanning;
import com.ercopac.ercopac_tracker.planning.repository.ProjectPlanningRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.dto.ProjectDetailsResponse;
import com.ercopac.ercopac_tracker.projects.dto.UpsertProjectRequest;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectPlanningRepository projectPlanningRepository;
    private final SecurityUtils securityUtils;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectPlanningRepository projectPlanningRepository,
                          SecurityUtils securityUtils) {
        this.projectRepository = projectRepository;
        this.projectPlanningRepository = projectPlanningRepository;
        this.securityUtils = securityUtils;
    }

    public ProjectDetailsResponse getProjectDetailsById(Long projectId) {
        Project project = getAccessibleProjectById(projectId);
        Optional<ProjectPlanning> planningOpt = projectPlanningRepository.findByProjectId(projectId);

        ProjectDetailsResponse response = new ProjectDetailsResponse();
        response.setId(project.getId());
        response.setCode(project.getCode());
        response.setName(project.getName());
        response.setShortName(project.getShortName());
        response.setPortfolio(project.getPortfolio());
        response.setOrgAssignment(project.getOrgAssignment());
        response.setCountry(project.getCountry());
        response.setProjectType(project.getProjectType());
        response.setProjectPhase(project.getProjectPhase());
        response.setPriority(project.getPriority());
        response.setPlannedStart(project.getPlannedStart());
        response.setPlannedEnd(project.getPlannedEnd());
        response.setProjectBudget(project.getProjectBudget());
        response.setTotalProjectBudget(project.getTotalProjectBudget());
        response.setProjectManagerId(project.getProjectManagerId());
        response.setComment(project.getComment());

        if (planningOpt.isPresent()) {
            ProjectPlanning planning = planningOpt.get();
            response.setExpectedStart(planning.getExpectedStart());
            response.setExpectedEnd(planning.getExpectedEnd());
            response.setProjectCalendar(planning.getProjectCalendar());
            response.setProbability(planning.getProbability());
            response.setKeywords(planning.getKeywords());
            response.setSubcontractors(planning.getSubcontractors());
        }

        return response;
    }

    private Project getAccessibleProjectById(Long projectId) {
        if (securityUtils.isPlatformUser()) {
            return projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        }

        Long orgId = requireCurrentOrganisationId();

        return projectRepository.findByIdAndOrganisationId(projectId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Project not accessible"));
    }

    private Long requireCurrentOrganisationId() {
        Long orgId = securityUtils.getCurrentOrganisationId();
        if (orgId == null) {
            throw new IllegalStateException("User has no organisation");
        }
        return orgId;
    }

    @Transactional
    public ProjectDashboardRowDto createProject(UpsertProjectRequest request) {
        Project project = new Project();
        applyRequest(project, request);

        if (!securityUtils.isPlatformUser()) {
            Long orgId = requireCurrentOrganisationId();
            Organisation organisation = new Organisation();
            organisation.setId(orgId);
            project.setOrganisation(organisation);
        }

        Project saved = projectRepository.save(project);
        return toDashboardDto(saved);
    }

    @Transactional
    public ProjectDashboardRowDto updateProject(Long id, UpsertProjectRequest request) {
        Project project = getAccessibleProjectById(id);
        applyRequest(project, request);

        if (!securityUtils.isPlatformUser()) {
            Long orgId = requireCurrentOrganisationId();
            if (project.getOrganisation() == null || !orgId.equals(project.getOrganisation().getId())) {
                throw new IllegalArgumentException("Project not accessible");
            }
        }

        Project saved = projectRepository.save(project);
        return toDashboardDto(saved);
    }

    @Transactional
    public void archiveProject(Long id) {
        Project project = getAccessibleProjectById(id);
        project.setArchived(true);
        project.setProjectPhase("ARCHIVED");
        projectRepository.save(project);
    }

    private void applyRequest(Project project, UpsertProjectRequest request) {
        project.setCode(request.getCode());
        project.setName(request.getName());
        project.setShortName(request.getShortName());
        project.setCustomer(request.getCustomer());
        project.setCategory(request.getCategory());
        project.setCountry(request.getCountry());
        project.setProjectType(request.getProjectType());
        project.setProjectPhase(request.getProjectPhase());
        project.setRiskLevel(request.getRiskLevel());
        project.setPlannedStart(request.getPlannedStart());
        project.setPlannedEnd(request.getPlannedEnd());
        project.setProjectBudget(request.getProjectBudget());
        project.setEstimatedCost(request.getEstimatedCost());
        project.setProjectManagerName(request.getProjectManagerName());
        project.setProgramManagerName(request.getProgramManagerName());
        project.setSalesManagerName(request.getSalesManagerName());
        project.setComment(request.getComment());
    }

    private ProjectDashboardRowDto toDashboardDto(Project p) {
        ProjectDashboardRowDto dto = new ProjectDashboardRowDto();
        dto.setId(p.getId());
        dto.setCode(p.getCode());
        dto.setName(p.getName());
        dto.setShortName(p.getShortName());
        dto.setCustomer(p.getCustomer());
        dto.setCategory(p.getCategory());
        dto.setCountry(p.getCountry());
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
        dto.setArchived(Boolean.TRUE.equals(p.getArchived()));
        return dto;
    }
}