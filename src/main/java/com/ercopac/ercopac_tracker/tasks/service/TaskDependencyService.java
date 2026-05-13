package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.dto.TaskDependencyDto;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskDependencyService {

    private final TaskDependencyRepository dependencyRepository;
    private final ProjectTaskRepository    taskRepository;
    private final TaskSchedulingService    taskSchedulingService;

    public TaskDependencyService(
            TaskDependencyRepository dependencyRepository,
            ProjectTaskRepository taskRepository,
            TaskSchedulingService taskSchedulingService) {
        this.dependencyRepository = dependencyRepository;
        this.taskRepository       = taskRepository;
        this.taskSchedulingService = taskSchedulingService;
    }

    // ── GET ALL FOR PROJECT ───────────────────────────────────────

    public List<TaskDependencyDto> getProjectDependencies(Long projectId) {
        return dependencyRepository.findByProjectId(projectId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── CREATE ────────────────────────────────────────────────────
    // After creating a dependency, cascade dates to the successor

    public TaskDependencyDto createDependency(TaskDependencyDto dto, Long projectId) {
        Long predId = dto.getPredecessorTaskId();
        Long succId = dto.getSuccessorTaskId();

        if (predId == null || succId == null)
            throw new IllegalArgumentException("predecessorTaskId and successorTaskId are required.");
        if (predId.equals(succId))
            throw new IllegalArgumentException("A task cannot depend on itself.");

        // Validate both tasks belong to this project
        ProjectTask pred = taskRepository.findById(predId)
                .orElseThrow(() -> new IllegalArgumentException("Predecessor task not found: " + predId));
        ProjectTask succ = taskRepository.findById(succId)
                .orElseThrow(() -> new IllegalArgumentException("Successor task not found: " + succId));

        if (!pred.getProjectId().equals(projectId) || !succ.getProjectId().equals(projectId))
            throw new IllegalArgumentException("Tasks must belong to project " + projectId);

        // Check for duplicate
        boolean exists = dependencyRepository.findByProjectId(projectId).stream()
                .anyMatch(d -> d.getPredecessorTaskId().equals(predId)
                            && d.getSuccessorTaskId().equals(succId));
        if (exists)
            throw new IllegalArgumentException("Dependency already exists.");

        TaskDependency dep = new TaskDependency();
        dep.setProjectId(projectId);
        dep.setPredecessorTaskId(predId);
        dep.setSuccessorTaskId(succId);
        dep.setDependencyType(dto.getDependencyType() != null
                ? dto.getDependencyType().toUpperCase() : "FS");
        dep.setLagDays(dto.getLagDays() != null ? dto.getLagDays() : 0);

        TaskDependency saved = dependencyRepository.save(dep);

        // ── KEY FIX: cascade dates from predecessor to successor ──
        taskSchedulingService.rescheduleFromTask(projectId, predId);

        return toDto(saved);
    }

    // ── UPDATE ────────────────────────────────────────────────────
    // After updating a dependency type/lag, re-cascade

    public TaskDependencyDto updateDependency(Long dependencyId, TaskDependencyDto dto, Long projectId) {
        TaskDependency dep = dependencyRepository.findById(dependencyId)
                .orElseThrow(() -> new IllegalArgumentException("Dependency not found: " + dependencyId));

        if (dto.getDependencyType() != null)
            dep.setDependencyType(dto.getDependencyType().toUpperCase());
        if (dto.getLagDays() != null)
            dep.setLagDays(dto.getLagDays());

        TaskDependency saved = dependencyRepository.save(dep);

        // Re-cascade with updated type/lag
        taskSchedulingService.rescheduleFromTask(projectId, dep.getPredecessorTaskId());

        return toDto(saved);
    }

    // ── DELETE ────────────────────────────────────────────────────

    public void deleteDependency(Long dependencyId, Long projectId) {
        TaskDependency dep = dependencyRepository.findById(dependencyId)
                .orElseThrow(() -> new IllegalArgumentException("Dependency not found: " + dependencyId));

        dependencyRepository.delete(dep);

        // Re-cascade after removing the constraint
        // (successor may now be free to move earlier)
        taskSchedulingService.rescheduleFromTask(projectId, dep.getPredecessorTaskId());
    }

    // ── MAP TO DTO ────────────────────────────────────────────────

    private TaskDependencyDto toDto(TaskDependency dep) {
        TaskDependencyDto dto = new TaskDependencyDto();
        dto.setId(dep.getId());
        dto.setPredecessorTaskId(dep.getPredecessorTaskId());
        dto.setSuccessorTaskId(dep.getSuccessorTaskId());
        dto.setDependencyType(dep.getDependencyType());
        dto.setLagDays(dep.getLagDays());
        return dto;
    }
}