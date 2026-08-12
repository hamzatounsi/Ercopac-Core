package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.dto.TaskDependencyDto;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
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

        validateDependencyChange(projectId, predId, succId, null);

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
        if (!projectId.equals(dep.getProjectId())) {
            throw new IllegalArgumentException("Dependency does not belong to the project.");
        }

        Long predecessorTaskId = dto.getPredecessorTaskId() != null
                ? dto.getPredecessorTaskId() : dep.getPredecessorTaskId();
        Long successorTaskId = dto.getSuccessorTaskId() != null
                ? dto.getSuccessorTaskId() : dep.getSuccessorTaskId();
        validateDependencyChange(projectId, predecessorTaskId, successorTaskId, dependencyId);

        dep.setPredecessorTaskId(predecessorTaskId);
        dep.setSuccessorTaskId(successorTaskId);

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
        if (!projectId.equals(dep.getProjectId())) {
            throw new IllegalArgumentException("Dependency does not belong to the project.");
        }

        dependencyRepository.delete(dep);

        // Re-cascade after removing the constraint
        // (successor may now be free to move earlier)
        taskSchedulingService.rescheduleFromTask(projectId, dep.getPredecessorTaskId());
    }

    private void validateDependencyChange(
            Long projectId,
            Long predecessorTaskId,
            Long successorTaskId,
            Long ignoredDependencyId) {
        if (predecessorTaskId == null || successorTaskId == null) {
            throw new IllegalArgumentException("predecessorTaskId and successorTaskId are required.");
        }
        if (predecessorTaskId.equals(successorTaskId)) {
            throw new IllegalArgumentException("A task cannot depend on itself.");
        }

        ProjectTask predecessor = taskRepository.findById(predecessorTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Predecessor task not found: " + predecessorTaskId));
        ProjectTask successor = taskRepository.findById(successorTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Successor task not found: " + successorTaskId));
        if (!projectId.equals(predecessor.getProjectId()) || !projectId.equals(successor.getProjectId())) {
            throw new IllegalArgumentException("Tasks must belong to project " + projectId);
        }

        List<TaskDependency> dependencies = dependencyRepository.findByProjectId(projectId);
        boolean duplicate = dependencies.stream()
                .filter(dependency -> !dependency.getId().equals(ignoredDependencyId))
                .anyMatch(dependency -> dependency.getPredecessorTaskId().equals(predecessorTaskId)
                        && dependency.getSuccessorTaskId().equals(successorTaskId));
        if (duplicate) throw new IllegalArgumentException("Dependency already exists.");

        Map<Long, List<Long>> successors = new HashMap<>();
        for (TaskDependency dependency : dependencies) {
            if (dependency.getId().equals(ignoredDependencyId)) continue;
            successors.computeIfAbsent(dependency.getPredecessorTaskId(), key -> new ArrayList<>())
                    .add(dependency.getSuccessorTaskId());
        }
        Deque<Long> pending = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        pending.add(successorTaskId);
        while (!pending.isEmpty()) {
            Long currentTaskId = pending.removeFirst();
            if (currentTaskId.equals(predecessorTaskId)) {
                throw new IllegalArgumentException("Dependency would create a circular relationship.");
            }
            if (visited.add(currentTaskId)) pending.addAll(successors.getOrDefault(currentTaskId, List.of()));
        }
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
