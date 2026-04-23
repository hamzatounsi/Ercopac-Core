package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.tasks.domain.TaskResourceAssignment;
import com.ercopac.ercopac_tracker.tasks.dto.TaskResourceAssignmentDto;
import com.ercopac.ercopac_tracker.tasks.repository.TaskResourceAssignmentRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskResourceAssignmentService {

    private final TaskResourceAssignmentRepository repository;
    private final UserRepository userRepository;

    public TaskResourceAssignmentService(
            TaskResourceAssignmentRepository repository,
            UserRepository userRepository
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public List<TaskResourceAssignmentDto> getTaskResources(Long projectId, Long taskId) {
        return repository.findByProjectIdAndTaskIdOrderByIdAsc(projectId, taskId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public TaskResourceAssignmentDto createTaskResource(Long projectId, Long taskId, TaskResourceAssignmentDto dto) {
        TaskResourceAssignment entity = new TaskResourceAssignment();
        entity.setProjectId(projectId);
        entity.setTaskId(taskId);
        apply(entity, dto);
        return toDto(repository.save(entity));
    }

    public TaskResourceAssignmentDto updateTaskResource(Long projectId, Long taskId, Long assignmentId, TaskResourceAssignmentDto dto) {
        TaskResourceAssignment entity = repository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Resource assignment not found with id: " + assignmentId));

        if (!entity.getProjectId().equals(projectId) || !entity.getTaskId().equals(taskId)) {
            throw new IllegalArgumentException("Resource assignment does not belong to the given project/task.");
        }

        apply(entity, dto);
        return toDto(repository.save(entity));
    }

    public void deleteTaskResource(Long projectId, Long taskId, Long assignmentId) {
        TaskResourceAssignment entity = repository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Resource assignment not found with id: " + assignmentId));

        if (!entity.getProjectId().equals(projectId) || !entity.getTaskId().equals(taskId)) {
            throw new IllegalArgumentException("Resource assignment does not belong to the given project/task.");
        }

        repository.delete(entity);
    }

    private void apply(TaskResourceAssignment entity, TaskResourceAssignmentDto dto) {
        entity.setResourceType(dto.getResourceType());
        entity.setAssignmentName(dto.getAssignmentName());
        entity.setQuantity(dto.getQuantity() == null ? 1 : dto.getQuantity());
        entity.setUnitsPercent(dto.getUnitsPercent() == null ? 100 : dto.getUnitsPercent());
        entity.setCost(dto.getCost());

        if (dto.getAssignedUserId() != null) {
            AppUser user = userRepository.findById(dto.getAssignedUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Assigned user not found with id: " + dto.getAssignedUserId()));
            entity.setAssignedUser(user);
        } else {
            entity.setAssignedUser(null);
        }
    }

    private TaskResourceAssignmentDto toDto(TaskResourceAssignment entity) {
        return new TaskResourceAssignmentDto()
                .setId(entity.getId())
                .setProjectId(entity.getProjectId())
                .setTaskId(entity.getTaskId())
                .setAssignedUserId(entity.getAssignedUser() != null ? entity.getAssignedUser().getId() : null)
                .setAssignedUserName(entity.getAssignedUser() != null ? entity.getAssignedUser().getFullName() : null)
                .setResourceType(entity.getResourceType())
                .setAssignmentName(entity.getAssignmentName())
                .setQuantity(entity.getQuantity())
                .setUnitsPercent(entity.getUnitsPercent())
                .setCost(entity.getCost());
    }
}