package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.dto.ProjectScheduleTaskResponse;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectScheduleService {

    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectRepository projectRepository;

    public ProjectScheduleService(ProjectTaskRepository projectTaskRepository,
                                  ProjectRepository projectRepository) {
        this.projectTaskRepository = projectTaskRepository;
        this.projectRepository = projectRepository;
    }

    public List<ProjectScheduleTaskResponse> getProjectSchedule(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project not found with id: " + projectId);
        }

        return projectTaskRepository.findByProjectIdOrderByDisplayOrderAscIdAsc(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ProjectScheduleTaskResponse mapToResponse(ProjectTask task) {
        return new ProjectScheduleTaskResponse()
                .setId(task.getId())
                .setProjectId(task.getProjectId())
                .setName(task.getName())
                .setDescription(task.getDescription())
                .setDurationDays(task.getDurationDays())
                .setBaselineStart(task.getBaselineStart())
                .setBaselineEnd(task.getBaselineEnd())
                .setPlannedStart(task.getPlannedStart())
                .setPlannedEnd(task.getPlannedEnd())
                .setPercentComplete(task.getPercentComplete())
                .setPriority(task.getPriority())
                .setTaskType(task.getTaskType())
                .setWbsCode(task.getWbsCode())
                .setDepartmentCode(task.getDepartmentCode())
                .setActive(task.getActive())
                .setDisplayOrder(task.getDisplayOrder())
                .setCustomerMilestone(task.getCustomerMilestone());
    }
}