package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTaskHistory;
import com.ercopac.ercopac_tracker.tasks.dto.ProjectTaskHistoryDto;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectTaskHistoryService {

    private final ProjectTaskHistoryRepository repository;

    public void logTaskUpdate(
            ProjectTask oldTask,
            ProjectTask newTask,
            Long organisationId,
            Long changedByUserId,
            String changedByName
    ) {
        logChange(newTask, "Name", oldTask.getName(), newTask.getName(), organisationId, changedByUserId, changedByName);
        logChange(newTask, "Planned Start", oldTask.getPlannedStart(), newTask.getPlannedStart(), organisationId, changedByUserId, changedByName);
        logChange(newTask, "Planned Finish", oldTask.getPlannedEnd(), newTask.getPlannedEnd(), organisationId, changedByUserId, changedByName);
        logChange(newTask, "Baseline Start", oldTask.getBaselineStart(), newTask.getBaselineStart(), organisationId, changedByUserId, changedByName);
        logChange(newTask, "Baseline Finish", oldTask.getBaselineEnd(), newTask.getBaselineEnd(), organisationId, changedByUserId, changedByName);
        logChange(newTask, "Duration", oldTask.getDurationDays(), newTask.getDurationDays(), organisationId, changedByUserId, changedByName);
        logChange(newTask, "Progress", oldTask.getPercentComplete(), newTask.getPercentComplete(), organisationId, changedByUserId, changedByName);
        logChange(newTask, "Department", oldTask.getDepartmentCode(), newTask.getDepartmentCode(), organisationId, changedByUserId, changedByName);
        logChange(newTask, "Resource Type", oldTask.getResourceType(), newTask.getResourceType(), organisationId, changedByUserId, changedByName);
        logChange(newTask, "Customer Milestone", oldTask.getCustomerMilestone(), newTask.getCustomerMilestone(), organisationId, changedByUserId, changedByName);
    }

    private void logChange(
            ProjectTask task,
            String fieldName,
            Object oldValue,
            Object newValue,
            Long organisationId,
            Long changedByUserId,
            String changedByName
    ) {
        if (Objects.equals(stringValue(oldValue), stringValue(newValue))) {
            return;
        }

        ProjectTaskHistory h = new ProjectTaskHistory();
        h.setOrganisationId(organisationId);
        h.setProjectId(task.getProjectId());
        h.setTaskId(task.getId());
        h.setTaskName(task.getName());
        h.setFieldName(fieldName);
        h.setOldValue(stringValue(oldValue));
        h.setNewValue(stringValue(newValue));
        h.setChangedByUserId(changedByUserId);
        h.setChangedByName(changedByName == null || changedByName.isBlank() ? "System" : changedByName);

        repository.save(h);
    }

    public List<ProjectTaskHistoryDto> getProjectHistory(Long organisationId, Long projectId) {
        return repository.findByOrganisationIdAndProjectIdOrderByChangedAtDesc(organisationId, projectId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<ProjectTaskHistoryDto> getTaskHistory(Long organisationId, Long projectId, Long taskId) {
        return repository.findByOrganisationIdAndProjectIdAndTaskIdOrderByChangedAtDesc(
                        organisationId,
                        projectId,
                        taskId
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ProjectTaskHistoryDto toDto(ProjectTaskHistory h) {
        return new ProjectTaskHistoryDto(
                h.getId(),
                h.getProjectId(),
                h.getTaskId(),
                h.getTaskName(),
                h.getFieldName(),
                h.getOldValue(),
                h.getNewValue(),
                h.getChangedByUserId(),
                h.getChangedByName(),
                h.getChangedAt()
        );
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}