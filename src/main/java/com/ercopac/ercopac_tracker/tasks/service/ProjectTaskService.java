package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.dto.ProjectScheduleTaskResponse;
import com.ercopac.ercopac_tracker.tasks.dto.UpdateProjectTaskRequest;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
public class ProjectTaskService {

    private final ProjectTaskRepository projectTaskRepository;

    public ProjectTaskService(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    public ProjectScheduleTaskResponse updateTask(Long taskId, UpdateProjectTaskRequest request) {
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        if (request.getPlannedStart() != null && request.getPlannedEnd() != null
                && request.getPlannedEnd().isBefore(request.getPlannedStart())) {
            throw new IllegalArgumentException("Planned end date cannot be before planned start date.");
        }

        if (request.getBaselineStart() != null && request.getBaselineEnd() != null
                && request.getBaselineEnd().isBefore(request.getBaselineStart())) {
            throw new IllegalArgumentException("Baseline end date cannot be before baseline start date.");
        }

        if (request.getPercentComplete() != null
                && (request.getPercentComplete() < 0 || request.getPercentComplete() > 100)) {
            throw new IllegalArgumentException("Percent complete must be between 0 and 100.");
        }

        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setBaselineStart(request.getBaselineStart());
        task.setBaselineEnd(request.getBaselineEnd());
        task.setPlannedStart(request.getPlannedStart());
        task.setPlannedEnd(request.getPlannedEnd());
        task.setPercentComplete(request.getPercentComplete());
        task.setPriority(request.getPriority());
        task.setTaskType(request.getTaskType());
        task.setWbsCode(request.getWbsCode());
        task.setDepartmentCode(request.getDepartmentCode());
        task.setActive(request.getActive());
        task.setDisplayOrder(request.getDisplayOrder());
        task.setCustomerMilestone(request.getCustomerMilestone());
        task.setScheduleMode(request.getScheduleMode());

        if (request.getDurationDays() != null) {
            task.setDurationDays(request.getDurationDays());
        } else if (request.getPlannedStart() != null && request.getPlannedEnd() != null) {
            int duration = (int) ChronoUnit.DAYS.between(request.getPlannedStart(), request.getPlannedEnd()) + 1;
            task.setDurationDays(Math.max(duration, 1));
        }

        ProjectTask saved = projectTaskRepository.save(task);

        return new ProjectScheduleTaskResponse()
                .setId(saved.getId())
                .setProjectId(saved.getProjectId())
                .setName(saved.getName())
                .setDescription(saved.getDescription())
                .setDurationDays(saved.getDurationDays())
                .setBaselineStart(saved.getBaselineStart())
                .setBaselineEnd(saved.getBaselineEnd())
                .setPlannedStart(saved.getPlannedStart())
                .setPlannedEnd(saved.getPlannedEnd())
                .setPercentComplete(saved.getPercentComplete())
                .setPriority(saved.getPriority())
                .setTaskType(saved.getTaskType())
                .setWbsCode(saved.getWbsCode())
                .setDepartmentCode(saved.getDepartmentCode())
                .setActive(saved.getActive())
                .setDisplayOrder(saved.getDisplayOrder())
                .setCustomerMilestone(saved.getCustomerMilestone());
    }
}