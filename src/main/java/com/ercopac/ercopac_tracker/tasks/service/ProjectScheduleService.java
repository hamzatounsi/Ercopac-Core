package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.dto.ProjectScheduleTaskResponse;
import com.ercopac.ercopac_tracker.tasks.dto.TaskDependencyDto;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectScheduleService {

    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectRepository projectRepository;
    private final TaskDependencyRepository dependencyRepository;

    public ProjectScheduleService(ProjectTaskRepository projectTaskRepository,
                                  ProjectRepository projectRepository,
                                  TaskDependencyRepository dependencyRepository) {
        this.projectTaskRepository = projectTaskRepository;
        this.projectRepository = projectRepository;
        this.dependencyRepository = dependencyRepository;
    }

    public List<ProjectScheduleTaskResponse> getProjectSchedule(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project not found with id: " + projectId);
        }

        List<ProjectTask> tasks = projectTaskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);

        List<TaskDependency> dependencies = dependencyRepository.findByProjectId(projectId);

        Map<Long, List<TaskDependency>> depsBySuccessor = dependencies.stream()
                .collect(Collectors.groupingBy(TaskDependency::getSuccessorTaskId));

        return tasks.stream()
                .map(task -> mapToResponse(task, depsBySuccessor.get(task.getId())))
                .toList();
    }

    private ProjectScheduleTaskResponse mapToResponse(
            ProjectTask task,
            List<TaskDependency> dependencies
    ) {
        List<TaskDependencyDto> dependencyDtos = dependencies != null
                ? dependencies.stream().map(this::toDto).toList()
                : Collections.emptyList();

        String predecessorLabel = buildPredecessorLabel(dependencies);

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
                .setActualStart(task.getActualStart())
                .setActualEnd(task.getActualEnd())
                .setPercentComplete(task.getPercentComplete())
                .setAllocationPercent(task.getAllocationPercent())
                .setPlannedHours(task.getPlannedHours())
                .setActualHours(task.getActualHours())
                .setPriority(task.getPriority())
                .setScheduleMode(task.getScheduleMode())
                .setStatus(task.getStatus())
                .setColor(task.getColor())
                .setTaskType(task.getTaskType())
                .setWbsCode(task.getWbsCode())
                .setDepartmentCode(task.getDepartmentCode())
                .setResourceType(task.getResourceType())
                .setActive(task.getActive())
                .setDisplayOrder(task.getDisplayOrder())
                .setOutlineLevel(task.getOutlineLevel())
                .setCustomerMilestone(task.getCustomerMilestone())
                .setAssignedUserId(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null)
                .setAssignedUserName(task.getAssignedUser() != null ? task.getAssignedUser().getFullName() : null)
                .setDependencies(dependencyDtos)
                .setPredecessorLabel(predecessorLabel);
    }

    private TaskDependencyDto toDto(TaskDependency entity) {
        TaskDependencyDto dto = new TaskDependencyDto();
        dto.setId(entity.getId());
        dto.setPredecessorTaskId(entity.getPredecessorTaskId());
        dto.setSuccessorTaskId(entity.getSuccessorTaskId());
        dto.setDependencyType(entity.getDependencyType());
        dto.setLagDays(entity.getLagDays());
        return dto;
    }

    private String buildPredecessorLabel(List<TaskDependency> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return "";
        }

        return dependencies.stream()
                .map(dep -> dep.getPredecessorTaskId() + dep.getDependencyType())
                .collect(Collectors.joining(", "));
    }
}