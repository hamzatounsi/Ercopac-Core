package com.ercopac.ercopac_tracker.projects.service;

import com.ercopac.ercopac_tracker.planning.domain.ProjectPlanning;
import com.ercopac.ercopac_tracker.planning.repository.ProjectPlanningRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.dto.ProjectDetailsResponse;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectPlanningRepository projectPlanningRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectPlanningRepository projectPlanningRepository) {
        this.projectRepository = projectRepository;
        this.projectPlanningRepository = projectPlanningRepository;
    }

    public ProjectDetailsResponse getProjectDetailsById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));

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
}