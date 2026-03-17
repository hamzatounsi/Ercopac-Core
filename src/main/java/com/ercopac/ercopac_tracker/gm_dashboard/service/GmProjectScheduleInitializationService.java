package com.ercopac.ercopac_tracker.gm_dashboard.service;
import com.ercopac.ercopac_tracker.gm_dashboard.dto.InitializeProjectScheduleRequest;
import com.ercopac.ercopac_tracker.gm_dashboard.dto.InitializedProjectResponse;
import com.ercopac.ercopac_tracker.planning.domain.ProjectPlanning;
import com.ercopac.ercopac_tracker.planning.repository.ProjectPlanningRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;

import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class GmProjectScheduleInitializationService {

    private final ProjectRepository projectRepository;
    private final ProjectPlanningRepository projectPlanningRepository;

    public GmProjectScheduleInitializationService(
            ProjectRepository projectRepository,
            ProjectPlanningRepository projectPlanningRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectPlanningRepository = projectPlanningRepository;
    }

    @Transactional
    public InitializedProjectResponse initializeProject(InitializeProjectScheduleRequest request) {
        validateRequest(request);

        Project project = new Project();
        project.setCode(request.getCode());
        project.setName(request.getName());
        project.setShortName(request.getShortName());
        project.setPortfolio(request.getPortfolio());
        project.setOrgAssignment(request.getOrgAssignment());
        project.setCountry(request.getCountry());
        project.setProjectType(request.getProjectType());
        project.setProjectPhase(request.getProjectPhase());
        project.setPriority(request.getPriority());
        project.setPlannedStart(request.getPlannedStart());
        project.setPlannedEnd(request.getPlannedEnd());
        project.setProjectBudget(request.getProjectBudget());
        project.setTotalProjectBudget(request.getTotalProjectBudget());
        project.setProjectManagerId(request.getProjectManagerId());
        project.setComment(request.getComment());

        Project savedProject = projectRepository.save(project);

        ProjectPlanning planning = new ProjectPlanning();
        planning.setProjectId(savedProject.getId());
        planning.setExpectedStart(request.getExpectedStart());
        planning.setExpectedEnd(request.getExpectedEnd());
        planning.setProjectCalendar(request.getProjectCalendar());
        planning.setProbability(request.getProbability());
        planning.setKeywords(request.getKeywords());
        planning.setSubcontractors(request.getSubcontractors());

        projectPlanningRepository.save(planning);

        return new InitializedProjectResponse(
                savedProject.getId(),
                savedProject.getCode(),
                savedProject.getName(),
                "Project schedule initialized successfully."
        );
    }

    private void validateRequest(InitializeProjectScheduleRequest request) {
        if (projectRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("A project with this code already exists.");
        }

        if (request.getPlannedStart() != null
                && request.getPlannedEnd() != null
                && request.getPlannedEnd().isBefore(request.getPlannedStart())) {
            throw new IllegalArgumentException("Planned end date cannot be before planned start date.");
        }

        if (request.getExpectedStart() != null
                && request.getExpectedEnd() != null
                && request.getExpectedEnd().isBefore(request.getExpectedStart())) {
            throw new IllegalArgumentException("Expected end date cannot be before expected start date.");
        }

        if (request.getProbability() != null
                && (request.getProbability() < 0 || request.getProbability() > 100)) {
            throw new IllegalArgumentException("Probability must be between 0 and 100.");
        }
    }
}