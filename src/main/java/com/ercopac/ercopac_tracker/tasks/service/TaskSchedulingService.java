package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

@Service
@Transactional
public class TaskSchedulingService {

    private final ProjectTaskRepository projectTaskRepository;
    private final TaskDependencyRepository taskDependencyRepository;

    public TaskSchedulingService(ProjectTaskRepository projectTaskRepository,
                                 TaskDependencyRepository taskDependencyRepository) {
        this.projectTaskRepository = projectTaskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
    }

    public void rescheduleFromTask(Long projectId, Long changedTaskId) {
        Queue<Long> queue = new LinkedList<>();
        Set<Long> visited = new HashSet<>();

        queue.add(changedTaskId);

        while (!queue.isEmpty()) {
            Long predecessorId = queue.poll();

            List<TaskDependency> outgoingDependencies =
                    taskDependencyRepository.findByProjectIdAndPredecessorTaskId(projectId, predecessorId);

            for (TaskDependency dependency : outgoingDependencies) {
                Long successorId = dependency.getSuccessorTaskId();

                if (successorId == null || visited.contains(successorId)) {
                    continue;
                }

                boolean changed = recalculateSuccessor(projectId, successorId);
                if (changed) {
                    queue.add(successorId);
                }

                visited.add(successorId);
            }
        }
    }

    public boolean recalculateSuccessor(Long projectId, Long successorTaskId) {
        ProjectTask successor = projectTaskRepository.findById(successorTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Successor task not found: " + successorTaskId));

        if (!Objects.equals(successor.getProjectId(), projectId)) {
            throw new IllegalArgumentException("Successor task does not belong to project " + projectId);
        }

        List<TaskDependency> incomingDependencies =
                taskDependencyRepository.findByProjectIdAndSuccessorTaskId(projectId, successorTaskId);

        if (incomingDependencies.isEmpty()) {
            return false;
        }

        if ("MANUAL".equalsIgnoreCase(successor.getScheduleMode())) {
            return false;
        }

        LocalDate originalStart = successor.getPlannedStart();
        LocalDate originalEnd = successor.getPlannedEnd();

        long duration = calculateDuration(successor);

        LocalDate requiredStart = null;
        LocalDate requiredEnd = null;

        for (TaskDependency dep : incomingDependencies) {
            Long predecessorTaskId = dep.getPredecessorTaskId();
            if (predecessorTaskId == null) {
                continue;
            }

            ProjectTask predecessor = projectTaskRepository.findById(predecessorTaskId)
                    .orElse(null);

            if (predecessor == null) {
                continue;
            }

            if (!Objects.equals(predecessor.getProjectId(), projectId)) {
                continue;
            }

            LocalDate predStart = predecessor.getPlannedStart();
            LocalDate predEnd = predecessor.getPlannedEnd() != null
                    ? predecessor.getPlannedEnd()
                    : predecessor.getPlannedStart();

            if (predStart == null && predEnd == null) {
                continue;
            }

            int lag = dep.getLagDays() != null ? dep.getLagDays() : 0;
            String type = dep.getDependencyType() != null ? dep.getDependencyType().trim().toUpperCase() : "FS";

            switch (type) {
                case "SS" -> {
                    if (predStart != null) {
                        LocalDate candidate = predStart.plusDays(lag);
                        requiredStart = max(requiredStart, candidate);
                    }
                }
                case "FF" -> {
                    if (predEnd != null) {
                        LocalDate candidate = predEnd.plusDays(lag);
                        requiredEnd = max(requiredEnd, candidate);
                    }
                }
                case "SF" -> {
                    if (predStart != null) {
                        LocalDate candidate = predStart.plusDays(lag);
                        requiredEnd = max(requiredEnd, candidate);
                    }
                }
                case "FS" -> {
                    if (predEnd != null) {
                        LocalDate candidate = predEnd.plusDays(lag);
                        requiredStart = max(requiredStart, candidate);
                    }
                }
                default -> {
                    if (predEnd != null) {
                        LocalDate candidate = predEnd.plusDays(lag);
                        requiredStart = max(requiredStart, candidate);
                    }
                }
            }
        }

        LocalDate newStart = originalStart;
        LocalDate newEnd = originalEnd;

        boolean milestone = "MILESTONE".equalsIgnoreCase(successor.getTaskType());

        if (milestone) {
            LocalDate milestoneDate = firstNonNull(requiredStart, requiredEnd, originalStart, originalEnd);
            if (milestoneDate == null) {
                return false;
            }

            newStart = milestoneDate;
            newEnd = milestoneDate;
            successor.setDurationDays(0);
        } else {
            if (requiredStart != null && requiredEnd != null) {
                newStart = requiredStart;
                newEnd = newStart.plusDays(Math.max(0, duration - 1));

                if (newEnd.isBefore(requiredEnd)) {
                    newEnd = requiredEnd;
                    newStart = newEnd.minusDays(Math.max(0, duration - 1));
                }
            } else if (requiredStart != null) {
                newStart = requiredStart;
                newEnd = newStart.plusDays(Math.max(0, duration - 1));
            } else if (requiredEnd != null) {
                newEnd = requiredEnd;
                newStart = newEnd.minusDays(Math.max(0, duration - 1));
            }
        }

        boolean changed = !Objects.equals(originalStart, newStart) || !Objects.equals(originalEnd, newEnd);
        if (!changed) {
            return false;
        }

        successor.setPlannedStart(newStart);
        successor.setPlannedEnd(newEnd);

        if (!milestone && newStart != null && newEnd != null) {
            successor.setDurationDays((int) ChronoUnit.DAYS.between(newStart, newEnd) + 1);
        }

        projectTaskRepository.save(successor);
        return true;
    }

    private long calculateDuration(ProjectTask task) {
        if ("MILESTONE".equalsIgnoreCase(task.getTaskType())) {
            return 0;
        }

        if (task.getPlannedStart() != null && task.getPlannedEnd() != null) {
            return Math.max(1, ChronoUnit.DAYS.between(task.getPlannedStart(), task.getPlannedEnd()) + 1);
        }

        return Math.max(1, task.getDurationDays() != null ? task.getDurationDays() : 1);
    }

    private LocalDate max(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    @SafeVarargs
    private final LocalDate firstNonNull(LocalDate... values) {
        for (LocalDate value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}