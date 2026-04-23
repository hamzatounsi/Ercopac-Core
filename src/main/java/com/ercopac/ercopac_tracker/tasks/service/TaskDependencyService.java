package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.dto.TaskDependencyDto;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskDependencyService {

    private final TaskDependencyRepository repository;
    private final TaskSchedulingService taskSchedulingService;

    public TaskDependencyService(
            TaskDependencyRepository repository,
            TaskSchedulingService taskSchedulingService
    ) {
        this.repository = repository;
        this.taskSchedulingService = taskSchedulingService;
    }

    public List<TaskDependencyDto> getProjectDependencies(Long projectId) {
        return repository.findByProjectId(projectId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TaskDependencyDto createDependency(TaskDependencyDto dto, Long projectId) {
        validateDependency(dto);

        boolean exists = repository.findByProjectId(projectId).stream()
                .anyMatch(dep ->
                        dep.getPredecessorTaskId().equals(dto.getPredecessorTaskId()) &&
                        dep.getSuccessorTaskId().equals(dto.getSuccessorTaskId()) &&
                        dep.getDependencyType().equalsIgnoreCase(normalizeType(dto.getDependencyType()))
                );

        if (exists) {
            throw new IllegalArgumentException("Dependency already exists.");
        }

        TaskDependency entity = new TaskDependency();
        entity.setProjectId(projectId);
        entity.setPredecessorTaskId(dto.getPredecessorTaskId());
        entity.setSuccessorTaskId(dto.getSuccessorTaskId());
        entity.setDependencyType(normalizeType(dto.getDependencyType()));
        entity.setLagDays(dto.getLagDays() == null ? 0 : dto.getLagDays());

        TaskDependency saved = repository.save(entity);

        // Recalculate successor dates starting from the predecessor
        taskSchedulingService.rescheduleFromTask(projectId, saved.getPredecessorTaskId());

        return toDto(saved);
    }

    public TaskDependencyDto updateDependency(Long dependencyId, TaskDependencyDto dto, Long projectId) {
        validateDependency(dto);

        boolean duplicateExists = repository.findByProjectId(projectId).stream()
                .anyMatch(dep ->
                        !dep.getId().equals(dependencyId) &&
                        dep.getPredecessorTaskId().equals(dto.getPredecessorTaskId()) &&
                        dep.getSuccessorTaskId().equals(dto.getSuccessorTaskId()) &&
                        dep.getDependencyType().equalsIgnoreCase(normalizeType(dto.getDependencyType()))
                );

        if (duplicateExists) {
            throw new IllegalArgumentException("Dependency already exists.");
        }

        TaskDependency entity = repository.findById(dependencyId)
                .orElseThrow(() -> new IllegalArgumentException("Dependency not found with id: " + dependencyId));

        if (!entity.getProjectId().equals(projectId)) {
            throw new IllegalArgumentException("Dependency does not belong to project " + projectId);
        }

        Long oldPredecessorTaskId = entity.getPredecessorTaskId();

        entity.setPredecessorTaskId(dto.getPredecessorTaskId());
        entity.setSuccessorTaskId(dto.getSuccessorTaskId());
        entity.setDependencyType(normalizeType(dto.getDependencyType()));
        entity.setLagDays(dto.getLagDays() == null ? 0 : dto.getLagDays());

        TaskDependency saved = repository.save(entity);

        // Recalculate using both old and new predecessor in case predecessor changed
        taskSchedulingService.rescheduleFromTask(projectId, oldPredecessorTaskId);
        if (!oldPredecessorTaskId.equals(saved.getPredecessorTaskId())) {
            taskSchedulingService.rescheduleFromTask(projectId, saved.getPredecessorTaskId());
        }

        return toDto(saved);
    }

    public void deleteDependency(Long dependencyId, Long projectId) {
        TaskDependency entity = repository.findById(dependencyId)
                .orElseThrow(() -> new IllegalArgumentException("Dependency not found with id: " + dependencyId));

        if (!entity.getProjectId().equals(projectId)) {
            throw new IllegalArgumentException("Dependency does not belong to project " + projectId);
        }

        Long predecessorTaskId = entity.getPredecessorTaskId();

        repository.delete(entity);

        // Recalculate after dependency removal
        taskSchedulingService.rescheduleFromTask(projectId, predecessorTaskId);
    }

    private void validateDependency(TaskDependencyDto dto) {
        if (dto.getPredecessorTaskId() == null || dto.getSuccessorTaskId() == null) {
            throw new IllegalArgumentException("Predecessor and successor task ids are required.");
        }

        if (dto.getPredecessorTaskId().equals(dto.getSuccessorTaskId())) {
            throw new IllegalArgumentException("A task cannot depend on itself.");
        }

        String type = normalizeType(dto.getDependencyType());
        if (!type.equals("FS") && !type.equals("SS") && !type.equals("FF") && !type.equals("SF")) {
            throw new IllegalArgumentException("Dependency type must be one of: FS, SS, FF, SF.");
        }
    }

    private String normalizeType(String type) {
        return type == null ? "FS" : type.trim().toUpperCase();
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
}