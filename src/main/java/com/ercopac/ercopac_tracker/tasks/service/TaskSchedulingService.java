package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FIX: TaskSchedulingService now calls rollupSummaries() after cascading
 * so parent SUMMARY tasks update their dates and progress automatically
 * whenever a child task moves due to a dependency.
 */
@Service
public class TaskSchedulingService {

    private final ProjectTaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;

    // @Lazy breaks the circular dependency with ProjectTaskService
    private final ProjectTaskService projectTaskService;

    public TaskSchedulingService(
            ProjectTaskRepository taskRepository,
            TaskDependencyRepository dependencyRepository,
            @Lazy ProjectTaskService projectTaskService
    ) {
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
        this.projectTaskService = projectTaskService;
    }

    /**
     * Starting from the given task, cascade date changes to all successors
     * using a topological BFS. Respects FS, SS, FF, SF dependency types
     * with lag. After cascading, runs rollupSummaries so SUMMARY parents update.
     */
    public void rescheduleFromTask(Long projectId, Long startTaskId) {
        List<ProjectTask> allTasks = taskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);

        if (allTasks.isEmpty()) return;

        List<TaskDependency> allDeps = dependencyRepository.findByProjectId(projectId);

        // Build successor map: predecessorId → list of dependencies where it is predecessor
        Map<Long, List<TaskDependency>> successorMap = allDeps.stream()
                .collect(Collectors.groupingBy(TaskDependency::getPredecessorTaskId));

        // BFS from the changed task
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(startTaskId);

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            if (visited.contains(currentId)) continue;
            visited.add(currentId);

            List<TaskDependency> outgoing = successorMap.getOrDefault(currentId, List.of());

            for (TaskDependency dep : outgoing) {
                Long successorId = dep.getSuccessorTaskId();
                ProjectTask successor = allTasks.stream()
                        .filter(t -> t.getId().equals(successorId))
                        .findFirst()
                        .orElse(null);

                if (successor == null) continue;

                // Skip manually-scheduled tasks
                if ("MANUAL".equalsIgnoreCase(successor.getScheduleMode())) continue;

                ProjectTask predecessor = allTasks.stream()
                        .filter(t -> t.getId().equals(currentId))
                        .findFirst()
                        .orElse(null);

                if (predecessor == null) continue;

                boolean changed = applyDependency(predecessor, successor, dep);

                if (changed) {
                    taskRepository.save(successor);
                    queue.add(successorId);
                }
            }
        }

        // FIX: after cascading, rollup summaries so parent dates update
        projectTaskService.rollupSummariesPublic(projectId);
    }

    /**
     * Apply a single dependency constraint and return true if successor dates changed.
     */
    private boolean applyDependency(ProjectTask pred, ProjectTask succ, TaskDependency dep) {
        if (pred.getPlannedEnd() == null && pred.getPlannedStart() == null) return false;

        String type = dep.getDependencyType() == null ? "FS" : dep.getDependencyType().toUpperCase();
        int lag = dep.getLagDays() == null ? 0 : dep.getLagDays();

        LocalDate predStart = pred.getPlannedStart();
        LocalDate predEnd   = pred.getPlannedEnd() != null ? pred.getPlannedEnd()
                                                           : pred.getPlannedStart();

        LocalDate newSuccStart = succ.getPlannedStart();
        LocalDate newSuccEnd   = succ.getPlannedEnd();

        int duration = succ.getDurationDays() != null ? Math.max(1, succ.getDurationDays()) : 1;
        boolean isMilestone = "MILESTONE".equalsIgnoreCase(succ.getTaskType());
        if (isMilestone) duration = 0;

        LocalDate requiredStart = null;
        LocalDate requiredEnd   = null;

        switch (type) {
            case "FS": // Finish-to-Start: successor starts after predecessor ends
                requiredStart = predEnd.plusDays(1+lag);
                break;
            case "SS": // Start-to-Start: successor starts after predecessor starts
                requiredStart = predStart.plusDays(lag);
                break;
            case "FF": // Finish-to-Finish: successor ends after predecessor ends
                requiredEnd = predEnd.plusDays(lag);
                break;
            case "SF": // Start-to-Finish: successor ends after predecessor starts
                requiredEnd = predStart.plusDays(lag);
                break;
            default:
                requiredStart = predEnd.plusDays(1+lag);
        }

        // Compute new dates for successor
        LocalDate computedStart = newSuccStart;
        LocalDate computedEnd   = newSuccEnd;

        if (requiredStart != null) {
            if (newSuccStart == null || requiredStart.isAfter(newSuccStart)) {
                computedStart = requiredStart;
                if (isMilestone) {
                    computedEnd = computedStart;
                } else {
                    computedEnd = computedStart.plusDays(duration - 1);
                }
            }
        } else if (requiredEnd != null) {
            if (newSuccEnd == null || requiredEnd.isAfter(newSuccEnd)) {
                computedEnd = requiredEnd;
                if (isMilestone) {
                    computedStart = computedEnd;
                } else {
                    computedStart = computedEnd.minusDays(duration - 1);
                }
            }
        }

        // Check if anything changed
        boolean changed = !Objects.equals(computedStart, newSuccStart)
                || !Objects.equals(computedEnd, newSuccEnd);

        if (changed) {
            succ.setPlannedStart(computedStart);
            succ.setPlannedEnd(computedEnd);
            if (!isMilestone && computedStart != null && computedEnd != null) {
                long days = computedStart.until(computedEnd, java.time.temporal.ChronoUnit.DAYS) + 1;
                succ.setDurationDays((int) Math.max(1, days));
            }
        }

        return changed;
    }
}