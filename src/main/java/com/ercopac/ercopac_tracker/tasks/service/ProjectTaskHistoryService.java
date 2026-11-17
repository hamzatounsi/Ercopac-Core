package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTaskHistory;
import com.ercopac.ercopac_tracker.tasks.dto.ProjectTaskHistoryDto;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskHistoryRepository;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectTaskHistoryService {

    private final ProjectTaskHistoryRepository repository;
    private final ProjectTaskRepository taskRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    public ProjectTaskHistoryDto addMyTaskComment(Long projectId, Long taskId, String text) {
        if (text == null || text.isBlank() || text.length() > 2000) {
            throw new IllegalArgumentException("Comment must be between 1 and 2000 characters");
        }
        Long organisationId = securityUtils.getCurrentOrganisationId();
        Long userId = securityUtils.getCurrentUserId();
        ProjectTask task = taskRepository.findAssignedTask(taskId, projectId, organisationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task not assigned to current employee"));
        ProjectTaskHistory comment = new ProjectTaskHistory();
        comment.setOrganisationId(organisationId);
        comment.setProjectId(projectId);
        comment.setTaskId(taskId);
        comment.setTaskName(task.getName());
        comment.setFieldName("Comment");
        comment.setNewValue(text.trim());
        comment.setChangedByUserId(userId);
        comment.setChangedByName(userRepository.findByIdAndOrganisation_Id(userId, organisationId)
                .map(user -> user.getFullName()).orElse("Employee"));
        return toDto(repository.save(comment));
    }

    public List<ProjectTaskHistoryDto> getMyTaskComments(Long projectId, Long taskId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        taskRepository.findAssignedTask(taskId, projectId, organisationId, securityUtils.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Task not assigned to current employee"));
        return repository.findByOrganisationIdAndProjectIdAndTaskIdAndFieldNameOrderByChangedAtDesc(
                organisationId, projectId, taskId, "Comment").stream().map(this::toDto).toList();
    }

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
