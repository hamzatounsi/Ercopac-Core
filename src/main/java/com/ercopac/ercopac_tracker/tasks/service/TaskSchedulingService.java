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
 * Cascades date changes across task dependencies using a topological BFS.
 *
 * Two independent cascades run for each dependency edge:
 *  1. Baseline/Planned cascade — the working schedule. The successor SLIDES
 *     with the predecessor in both directions (earlier or later), not just
 *     pushed forward.
 *  2. Actual cascade — derived from the PREDECESSOR's actual dates, not
 *     baseline. Only runs if the predecessor actually has real progress
 *     recorded (actualStart/actualEnd set). Same bidirectional slide.
 *
 * After cascading, runs rollupSummaries so SUMMARY parents update.
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

                boolean plannedChanged = applyDependency(predecessor, successor, dep);
                boolean actualChanged = applyActualDependency(predecessor, successor, dep);

                if (plannedChanged || actualChanged) {
                    taskRepository.save(successor);
                    queue.add(successorId);
                }
            }
        }

        // FIX: after cascading, rollup summaries so parent dates update
        projectTaskService.rollupSummariesPublic(projectId);
    }

    /**
     * Apply a single dependency constraint to baseline/planned dates and
     * return true if successor dates changed.
     *
     * FIX: the successor now SLIDES with the predecessor in both directions
     * (earlier or later), instead of only ever being pushed forward.
     * Note: with multiple predecessors on the same successor, whichever
     * dependency edge is processed last in the BFS will win — this keeps
     * the behavior simple and correct for the common single-predecessor
     * chain case.
     */
    private boolean applyDependency(ProjectTask pred, ProjectTask succ, TaskDependency dep) {
        LocalDate predStart = pred.getBaselineStart() != null ? pred.getBaselineStart() : pred.getPlannedStart();
        LocalDate predEndRaw = pred.getBaselineEnd() != null ? pred.getBaselineEnd() : pred.getPlannedEnd();
        if (predStart == null && predEndRaw == null) return false;
        LocalDate predEnd = predEndRaw != null ? predEndRaw : predStart;

        String type = dep.getDependencyType() == null ? "FS" : dep.getDependencyType().toUpperCase();
        int lag = dep.getLagDays() == null ? 0 : dep.getLagDays();

        // Use baseline dates as source of truth (what the UI actually shows/edits),
        // not plannedStart/plannedEnd, which can silently diverge from baseline.
        LocalDate newSuccStart = succ.getBaselineStart() != null ? succ.getBaselineStart() : succ.getPlannedStart();
        LocalDate newSuccEnd   = succ.getBaselineEnd() != null ? succ.getBaselineEnd() : succ.getPlannedEnd();

        int duration = succ.getDurationDays() != null ? Math.max(1, succ.getDurationDays()) : 1;
        boolean isMilestone = "MILESTONE".equalsIgnoreCase(succ.getTaskType());
        if (isMilestone) duration = 0;

        LocalDate requiredStart = null;
        LocalDate requiredEnd   = null;

        switch (type) {
            case "FS": requiredStart = predEnd.plusDays(1 + lag); break;
            case "SS": requiredStart = predStart.plusDays(lag); break;
            case "FF": requiredEnd = predEnd.plusDays(lag); break;
            case "SF": requiredEnd = predStart.plusDays(lag); break;
            default:   requiredStart = predEnd.plusDays(1 + lag);
        }

        LocalDate computedStart = newSuccStart;
        LocalDate computedEnd   = newSuccEnd;

        if (requiredStart != null) {
            computedStart = requiredStart;
            computedEnd = isMilestone ? computedStart : computedStart.plusDays(duration - 1);
        } else if (requiredEnd != null) {
            computedEnd = requiredEnd;
            computedStart = isMilestone ? computedEnd : computedEnd.minusDays(duration - 1);
        }

        boolean changed = !Objects.equals(computedStart, newSuccStart)
                || !Objects.equals(computedEnd, newSuccEnd);

        if (changed) {
            // Write to BOTH baseline and planned so the UI (which reads baseline) updates
            succ.setBaselineStart(computedStart);
            succ.setBaselineEnd(computedEnd);
            succ.setPlannedStart(computedStart);
            succ.setPlannedEnd(computedEnd);
            if (!isMilestone && computedStart != null && computedEnd != null) {
                long days = computedStart.until(computedEnd, java.time.temporal.ChronoUnit.DAYS) + 1;
                succ.setDurationDays((int) Math.max(1, days));
            }
        }

        return changed;
    }

    /**
     * Apply a single dependency constraint to ACTUAL dates and return true
     * if the successor's actual dates changed.
     *
     * This cascade is independent from the baseline/planned one above and
     * uses the PREDECESSOR's actual dates as the source of truth. It only
     * runs when the predecessor has real actual progress recorded — if the
     * predecessor hasn't actually started/finished, there is nothing real
     * yet to cascade onto the successor.
     *
     * FIX: same bidirectional slide as the baseline/planned cascade — the
     * successor's actual dates follow the predecessor's actual dates in
     * both directions (earlier or later), not just pushed forward.
     */
    private boolean applyActualDependency(ProjectTask pred, ProjectTask succ, TaskDependency dep) {
        if (pred.getActualStart() == null && pred.getActualEnd() == null) return false;

        String type = dep.getDependencyType() == null ? "FS" : dep.getDependencyType().toUpperCase();
        int lag = dep.getLagDays() == null ? 0 : dep.getLagDays();

        LocalDate predActualStart = pred.getActualStart();
        LocalDate predActualEnd   = pred.getActualEnd() != null ? pred.getActualEnd() : pred.getActualStart();

        LocalDate newSuccActualStart = succ.getActualStart();
        LocalDate newSuccActualEnd   = succ.getActualEnd();

        boolean isMilestone = "MILESTONE".equalsIgnoreCase(succ.getTaskType());

        // Duration for the successor's actual span: prefer its own existing
        // actual span if both dates are already set, otherwise fall back to
        // the task's planned durationDays.
        int duration;
        if (!isMilestone && newSuccActualStart != null && newSuccActualEnd != null) {
            long days = newSuccActualStart.until(newSuccActualEnd, java.time.temporal.ChronoUnit.DAYS) + 1;
            duration = (int) Math.max(1, days);
        } else {
            duration = succ.getDurationDays() != null ? Math.max(1, succ.getDurationDays()) : 1;
        }
        if (isMilestone) duration = 0;

        LocalDate requiredStart = null;
        LocalDate requiredEnd   = null;

        switch (type) {
            case "FS":
                if (predActualEnd != null) requiredStart = predActualEnd.plusDays(1 + lag);
                break;
            case "SS":
                if (predActualStart != null) requiredStart = predActualStart.plusDays(lag);
                break;
            case "FF":
                if (predActualEnd != null) requiredEnd = predActualEnd.plusDays(lag);
                break;
            case "SF":
                if (predActualStart != null) requiredEnd = predActualStart.plusDays(lag);
                break;
            default:
                if (predActualEnd != null) requiredStart = predActualEnd.plusDays(1 + lag);
        }

        if (requiredStart == null && requiredEnd == null) return false;

        LocalDate computedStart = newSuccActualStart;
        LocalDate computedEnd   = newSuccActualEnd;

        if (requiredStart != null) {
            computedStart = requiredStart;
            computedEnd = isMilestone ? computedStart : computedStart.plusDays(duration - 1);
        } else if (requiredEnd != null) {
            computedEnd = requiredEnd;
            computedStart = isMilestone ? computedEnd : computedEnd.minusDays(duration - 1);
        }

        boolean changed = !Objects.equals(computedStart, newSuccActualStart)
                || !Objects.equals(computedEnd, newSuccActualEnd);

        if (changed) {
            succ.setActualStart(computedStart);
            succ.setActualEnd(computedEnd);
        }

        return changed;
    }
}