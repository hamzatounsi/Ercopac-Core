package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.dto.CreateTaskRequest;
import com.ercopac.ercopac_tracker.tasks.dto.ProjectScheduleTaskResponse;
import com.ercopac.ercopac_tracker.tasks.dto.ResourceUserDto;
import com.ercopac.ercopac_tracker.tasks.dto.TaskDependencyDto;
import com.ercopac.ercopac_tracker.tasks.dto.UpdateProjectTaskRequest;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskResourceAssignmentRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectTaskService {

    private final ProjectRepository                projectRepository;
    private final ProjectTaskRepository            projectTaskRepository;
    private final UserRepository                   userRepository;
    private final TaskDependencyRepository         taskDependencyRepository;
    private final TaskResourceAssignmentRepository taskResourceAssignmentRepository;
    private final TaskSchedulingService            taskSchedulingService;
    private final ProjectTaskHistoryService        historyService;

    public ProjectTaskService(
            ProjectTaskRepository projectTaskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TaskDependencyRepository taskDependencyRepository,
            TaskResourceAssignmentRepository taskResourceAssignmentRepository,
            TaskSchedulingService taskSchedulingService,
            ProjectTaskHistoryService historyService) {
        this.projectTaskRepository            = projectTaskRepository;
        this.projectRepository                = projectRepository;
        this.userRepository                   = userRepository;
        this.taskDependencyRepository         = taskDependencyRepository;
        this.taskResourceAssignmentRepository = taskResourceAssignmentRepository;
        this.taskSchedulingService            = taskSchedulingService;
        this.historyService                   = historyService;
    }

    // ══════════════════════════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════
    public ProjectScheduleTaskResponse updateTask(Long taskId, UpdateProjectTaskRequest request) {
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        ProjectTask oldTask = copyForHistory(task);        

        validateDates(request);
        validatePercent(request.getPercentComplete(), "percentComplete");

        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setTaskType(request.getTaskType());
        task.setBaselineStart(request.getBaselineStart());
        task.setBaselineEnd(request.getBaselineEnd());
        task.setPlannedStart(request.getPlannedStart());
        task.setPlannedEnd(request.getPlannedEnd());
        task.setActualStart(request.getActualStart());
        task.setActualEnd(request.getActualEnd());
        task.setPercentComplete(request.getPercentComplete());
        task.setAllocationPercent(request.getAllocationPercent());
        task.setPriority(request.getPriority());
        task.setWbsCode(request.getWbsCode());
        task.setDepartmentCode(request.getDepartmentCode());
        task.setResourceType(request.getResourceType());
        task.setActive(request.getActive());
        task.setDisplayOrder(request.getDisplayOrder());
        task.setOutlineLevel(request.getOutlineLevel());
        task.setCustomerMilestone(request.getCustomerMilestone());
        task.setScheduleMode(request.getScheduleMode());
        task.setStatus(request.getStatus());
        task.setColor(request.getColor());

        if (request.getParentId() != null) {
            task.setParentId(request.getParentId());
        }

        if (request.getAssignedUserId() != null) {
            userRepository.findById(request.getAssignedUserId())
                    .ifPresent(task::setAssignedUser);
        } else {
            task.setAssignedUser(null);
        }

        resolveDatesAndDuration(task, request);
        normalizeMilestone(task);

        Long projectId = task.getProjectId();

        historyService.logTaskUpdate(
                oldTask,
                task,
                getOrganisationIdFromSecurityContext(),
                getUserIdFromSecurityContext(),
                getUsernameFromSecurityContext()
        );

        ProjectTask saved = projectTaskRepository.save(task);

        rebuildParentIds(projectId);
        rollupSummaries(projectId);
        taskSchedulingService.rescheduleFromTask(projectId, saved.getId());

        return mapToResponse(projectTaskRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + saved.getId())));
    }

    // ══════════════════════════════════════════════════════════════
    // CREATE BELOW
    // ══════════════════════════════════════════════════════════════

    public ProjectScheduleTaskResponse createTaskBelow(Long projectId, Long afterTaskId,
                                                        CreateTaskRequest request) {
        ProjectTask anchor = projectTaskRepository.findById(afterTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Anchor task not found: " + afterTaskId));

        List<ProjectTask> allTasks = projectTaskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);

        List<ProjectTask> subtree = collectSubtree(anchor, allTasks);
        int insertAt = subtree.stream()
                .mapToInt(t -> t.getDisplayOrder() != null ? t.getDisplayOrder() : 0)
                .max().orElse(anchor.getDisplayOrder() != null ? anchor.getDisplayOrder() : 0);

        final int finalInsertAt = insertAt;

        allTasks.stream()
                .filter(t -> (t.getDisplayOrder() != null ? t.getDisplayOrder() : 0) > finalInsertAt)
                .forEach(t -> {
                    t.setDisplayOrder(t.getDisplayOrder() + 1);
                    projectTaskRepository.save(t);
                });

        ProjectTask task = new ProjectTask();
        task.setProjectId(projectId);
        task.setOrganisationId(anchor.getOrganisationId());
        task.setName(request.getName() != null ? request.getName() : "New Task");
        task.setTaskType("ACTIVITY");
        task.setPlannedStart(request.getPlannedStart());
        task.setPlannedEnd(request.getPlannedEnd() != null
                ? request.getPlannedEnd() : request.getPlannedStart());
        task.setDurationDays(request.getDurationDays() != null ? request.getDurationDays() : 1);
        task.setPercentComplete(0);
        task.setPriority(500);
        task.setScheduleMode("AUTO");
        task.setActive(true);
        task.setDisplayOrder(finalInsertAt + 1);
        task.setParentId(anchor.getParentId());
        task.setOutlineLevel(anchor.getOutlineLevel() != null ? anchor.getOutlineLevel() : 1);

        normalizeMilestone(task);
        ProjectTask saved = projectTaskRepository.save(task);

        rebuildStructureFromParentId(projectId);
        rollupSummaries(projectId);

        return mapToResponse(projectTaskRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found after insert: " + saved.getId())));
    }

    // ══════════════════════════════════════════════════════════════
    // COPY BELOW — copies task + full subtree with correct hierarchy
    // Example: Summary 1 (1.1, 1.2, 1.2.1) → Summary 2 (2.1, 2.2, 2.2.1)
    // ══════════════════════════════════════════════════════════════

 // REPLACE copyTaskBelow() in ProjectTaskService.java

    public ProjectScheduleTaskResponse copyTaskBelow(Long projectId, Long sourceTaskId) {

        // Load ALL tasks fresh from DB
        List<ProjectTask> allTasks = projectTaskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);

        // Find source FROM the fresh list — same Hibernate session, consistent state
        ProjectTask source = allTasks.stream()
                .filter(t -> sourceTaskId.equals(t.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + sourceTaskId));

        // Use source from allTasks — guaranteed correct displayOrder
        int sourceLevel        = source.getOutlineLevel() != null ? source.getOutlineLevel() : 1;
        int sourceDisplayOrder = source.getDisplayOrder() != null ? source.getDisplayOrder() : 0;

        System.out.println("=== SOURCE: id=" + source.getId()
                + " name=" + source.getName()
                + " level=" + sourceLevel
                + " order=" + sourceDisplayOrder);

        // Collect subtree: source + all tasks that immediately follow
        // with outlineLevel > sourceLevel
        List<ProjectTask> subtree = new ArrayList<>();

        for (ProjectTask t : allTasks) {  // already sorted by displayOrder
            int tOrder = t.getDisplayOrder() != null ? t.getDisplayOrder() : 0;
            int tLevel = t.getOutlineLevel() != null ? t.getOutlineLevel() : 1;

            if (tOrder < sourceDisplayOrder) continue; // before source

            if (tOrder == sourceDisplayOrder) {
                subtree.add(t);
                continue;
            }

            // tOrder > sourceDisplayOrder
            if (tLevel > sourceLevel) {
                subtree.add(t); // descendant
            } else {
                break; // same or higher level = end of subtree
            }
        }

        System.out.println("=== COPY subtree size: " + subtree.size());
        subtree.forEach(t -> System.out.println(
                "  id=" + t.getId()
                + " | " + t.getName()
                + " | level=" + t.getOutlineLevel()
                + " | order=" + t.getDisplayOrder()
                + " | parentId=" + t.getParentId()));

        int subtreeSize = subtree.size();

        int lastOrder = subtree.stream()
                .mapToInt(t -> t.getDisplayOrder() != null ? t.getDisplayOrder() : 0)
                .max().orElse(sourceDisplayOrder);

        // Shift tasks after the subtree down by subtreeSize
        for (ProjectTask t : allTasks) {
            int tOrder = t.getDisplayOrder() != null ? t.getDisplayOrder() : 0;
            boolean inSubtree = subtree.stream().anyMatch(s -> s.getId().equals(t.getId()));
            if (!inSubtree && tOrder > lastOrder) {
                t.setDisplayOrder(tOrder + subtreeSize);
                projectTaskRepository.save(t);
            }
        }

        // Save copies — index i → savedList[i]
        // For each child, scan backwards in subtree for nearest task with level = myLevel-1
        List<ProjectTask> savedList = new ArrayList<>();

        for (int i = 0; i < subtreeSize; i++) {
            ProjectTask orig = subtree.get(i);
            ProjectTask copy = new ProjectTask();

            copy.setProjectId(projectId);
            copy.setOrganisationId(orig.getOrganisationId());
            copy.setName(orig.getName() + (i == 0 ? " (Copy)" : ""));
            copy.setDescription(orig.getDescription());
            copy.setTaskType(orig.getTaskType());
            copy.setBaselineStart(orig.getBaselineStart());
            copy.setBaselineEnd(orig.getBaselineEnd());
            copy.setPlannedStart(orig.getPlannedStart());
            copy.setPlannedEnd(orig.getPlannedEnd());
            copy.setDurationDays(orig.getDurationDays());
            copy.setPercentComplete(0);
            copy.setPriority(orig.getPriority());
            copy.setWbsCode(null);
            copy.setDepartmentCode(orig.getDepartmentCode());
            copy.setResourceType(orig.getResourceType());
            copy.setScheduleMode(orig.getScheduleMode() != null ? orig.getScheduleMode() : "AUTO");
            copy.setActive(orig.getActive());
            copy.setDisplayOrder(lastOrder + 1 + i);
            copy.setCustomerMilestone(orig.getCustomerMilestone());
            copy.setAssignedUser(orig.getAssignedUser());
            copy.setAllocationPercent(orig.getAllocationPercent());
            copy.setStatus(orig.getStatus());
            copy.setColor(orig.getColor());
            copy.setOutlineLevel(orig.getOutlineLevel());

            // Set parentId
            if (i == 0) {
                copy.setParentId(source.getParentId()); // root copy → same parent as source
            } else {
                int myLevel     = orig.getOutlineLevel() != null ? orig.getOutlineLevel() : 1;
                int parentLevel = myLevel - 1;
                ProjectTask parentCopy = null;
                for (int j = i - 1; j >= 0; j--) {
                    int jLevel = subtree.get(j).getOutlineLevel() != null
                            ? subtree.get(j).getOutlineLevel() : 1;
                    if (jLevel == parentLevel) {
                        parentCopy = savedList.get(j);
                        break;
                    }
                }
                copy.setParentId(parentCopy != null ? parentCopy.getId() : source.getParentId());
            }

            normalizeMilestone(copy);
            ProjectTask saved = projectTaskRepository.save(copy);
            savedList.add(saved);

            System.out.println("  SAVED[" + i + "]: id=" + saved.getId()
                    + " | " + saved.getName()
                    + " | parentId=" + saved.getParentId()
                    + " | level=" + saved.getOutlineLevel());
        }

        rebuildStructureFromParentId(projectId);
        rollupSummaries(projectId);

        return mapToResponse(projectTaskRepository.findById(savedList.get(0).getId())
                .orElseThrow(() -> new IllegalArgumentException("Root copy not found")));
    }

    // ══════════════════════════════════════════════════════════════
    // DELETE — recursively deletes children first
    // ══════════════════════════════════════════════════════════════

    public void deleteTask(Long taskId) {
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        Long projectId = task.getProjectId();
        List<ProjectTask> allTasks = projectTaskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);

        List<ProjectTask> subtree = collectSubtree(task, allTasks);
        List<ProjectTask> reversed = new ArrayList<>(subtree);
        Collections.reverse(reversed);

        for (ProjectTask t : reversed) {
            taskDependencyRepository.deleteByPredecessorTaskId(t.getId());
            taskDependencyRepository.deleteBySuccessorTaskId(t.getId());
            taskResourceAssignmentRepository.deleteByProjectIdAndTaskId(projectId, t.getId());
            projectTaskRepository.delete(t);
        }

        rebuildStructureFromParentId(projectId);
        rollupSummaries(projectId);
    }

    // ══════════════════════════════════════════════════════════════
    // RESOURCE USERS
    // ══════════════════════════════════════════════════════════════

    public List<ResourceUserDto> getResourceUsersForProject(Long projectId) {
        List<ProjectTask> tasks = projectTaskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);

        Long orgId = tasks.stream()
                .filter(t -> t.getOrganisationId() != null)
                .map(ProjectTask::getOrganisationId)
                .findFirst()
                .orElse(null);

        if (orgId == null) {
            orgId = projectRepository.findById(projectId)
                    .map(p -> p.getOrganisation() != null ? p.getOrganisation().getId() : null)
                    .orElse(null);
        }

        if (orgId == null) return List.of();
        return userRepository.findByOrganisation_IdOrderByFullNameAsc(orgId)
                .stream()
                .map(u -> new ResourceUserDto(
                        u.getId(),
                        u.getFullName(),
                        u.getResourceType() != null ? u.getResourceType().getCode() : "",
                        u.getDepartmentCode() != null ? u.getDepartmentCode() : "",
                        u.getColor() != null ? u.getColor() : "#3b82f6"
                ))
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // ROLLUP SUMMARIES — bottom-up weighted average
    // ══════════════════════════════════════════════════════════════

    public void rollupSummariesPublic(Long projectId) {
        rollupSummaries(projectId);
    }

    private void rollupSummaries(Long projectId) {
        List<ProjectTask> tasks = projectTaskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);
        if (tasks.isEmpty()) return;

        int maxLevel = tasks.stream()
                .mapToInt(t -> t.getOutlineLevel() != null ? t.getOutlineLevel() : 1)
                .max().orElse(1);

        for (int level = maxLevel; level >= 1; level--) {
            final int currentLevel = level;
            List<ProjectTask> summaries = tasks.stream()
                    .filter(t -> "SUMMARY".equalsIgnoreCase(t.getTaskType()))
                    .filter(t -> Objects.equals(
                            t.getOutlineLevel() != null ? t.getOutlineLevel() : 1, currentLevel))
                    .collect(Collectors.toList());

            for (ProjectTask summary : summaries) {
                List<ProjectTask> children = getDirectChildren(summary, tasks);
                if (children.isEmpty()) continue;

                Optional<LocalDate> minStart = children.stream()
                        .map(ProjectTask::getPlannedStart).filter(Objects::nonNull)
                        .min(Comparator.naturalOrder());
                Optional<LocalDate> maxEnd = children.stream()
                        .map(ProjectTask::getPlannedEnd).filter(Objects::nonNull)
                        .max(Comparator.naturalOrder());

                int totalDuration = children.stream()
                        .mapToInt(c -> c.getDurationDays() != null ? c.getDurationDays() : 0).sum();

                long totalWeight = children.stream()
                        .mapToLong(c -> c.getDurationDays() != null ? c.getDurationDays() : 1).sum();
                double weightedPct = children.stream()
                        .mapToDouble(c -> {
                            int dur = c.getDurationDays() != null ? c.getDurationDays() : 1;
                            int pct = c.getPercentComplete() != null ? c.getPercentComplete() : 0;
                            return (double) dur * pct;
                        }).sum();
                int avgPct = totalWeight > 0 ? (int) Math.round(weightedPct / totalWeight) : 0;

                minStart.ifPresent(summary::setPlannedStart);
                maxEnd.ifPresent(summary::setPlannedEnd);
                summary.setDurationDays(totalDuration);
                summary.setPercentComplete(avgPct);
                projectTaskRepository.save(summary);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // REBUILD STRUCTURE FROM PARENT ID
    // Builds WBS codes using parentId FK — correct for all cases
    // ══════════════════════════════════════════════════════════════

    private void rebuildStructureFromParentId(Long projectId) {
        List<ProjectTask> tasks = projectTaskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);

        // Sequential displayOrder
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setDisplayOrder(i + 1);
        }

        // parentId → child counter
        Map<Long, Integer> childCounters = new HashMap<>();
        final Long ROOT_KEY = -1L;

        for (ProjectTask task : tasks) {
            Long parentId  = task.getParentId();
            Long counterKey = parentId != null ? parentId : ROOT_KEY;

            int count = childCounters.getOrDefault(counterKey, 0) + 1;
            childCounters.put(counterKey, count);

            if (parentId == null) {
                task.setWbsCode(String.valueOf(count));
                task.setOutlineLevel(1);
            } else {
                ProjectTask parent = tasks.stream()
                        .filter(t -> t.getId().equals(parentId))
                        .findFirst().orElse(null);

                if (parent != null && parent.getWbsCode() != null) {
                    task.setWbsCode(parent.getWbsCode() + "." + count);
                    task.setOutlineLevel(
                            (int) parent.getWbsCode().chars().filter(c -> c == '.').count() + 2);
                } else {
                    task.setWbsCode(String.valueOf(count));
                    task.setOutlineLevel(1);
                }
            }

            projectTaskRepository.save(task);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // REBUILD PARENT IDs — sets parentId from wbsCode
    // ══════════════════════════════════════════════════════════════

    private void rebuildParentIds(Long projectId) {
        List<ProjectTask> tasks = projectTaskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);

        Map<String, ProjectTask> wbsMap = tasks.stream()
                .filter(t -> t.getWbsCode() != null && !t.getWbsCode().isEmpty())
                .collect(Collectors.toMap(ProjectTask::getWbsCode, t -> t, (a, b) -> a));

        for (ProjectTask task : tasks) {
            String wbs = task.getWbsCode();
            if (wbs == null || !wbs.contains(".")) {
                task.setParentId(null);
            } else {
                String parentWbs = wbs.substring(0, wbs.lastIndexOf('.'));
                ProjectTask parent = wbsMap.get(parentWbs);
                task.setParentId(parent != null ? parent.getId() : null);
            }
            projectTaskRepository.save(task);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // COLLECT SUBTREE — by outlineLevel + displayOrder
    // ══════════════════════════════════════════════════════════════

    private List<ProjectTask> collectSubtree(ProjectTask root, List<ProjectTask> allTasks) {
        List<ProjectTask> result = new ArrayList<>();
        result.add(root);

        int rootLevel = root.getOutlineLevel() != null ? root.getOutlineLevel() : 1;

        List<ProjectTask> sorted = allTasks.stream()
                .sorted(Comparator.comparingInt(
                        t -> t.getDisplayOrder() != null ? t.getDisplayOrder() : 0))
                .collect(Collectors.toList());

        boolean collecting = false;
        for (ProjectTask t : sorted) {
            if (t.getId().equals(root.getId())) { collecting = true; continue; }
            if (collecting) {
                int tLevel = t.getOutlineLevel() != null ? t.getOutlineLevel() : 1;
                if (tLevel > rootLevel) result.add(t);
                else break;
            }
        }
        return result;
    }

    // ══════════════════════════════════════════════════════════════
    // GET DIRECT CHILDREN
    // ══════════════════════════════════════════════════════════════

    private List<ProjectTask> getDirectChildren(ProjectTask parent, List<ProjectTask> allTasks) {
        // Prefer parentId lookup
        List<ProjectTask> byParentId = allTasks.stream()
                .filter(t -> parent.getId().equals(t.getParentId()))
                .collect(Collectors.toList());
        if (!byParentId.isEmpty()) return byParentId;

        // Fallback: outlineLevel + displayOrder
        int parentLevel = parent.getOutlineLevel() != null ? parent.getOutlineLevel() : 1;
        int childLevel  = parentLevel + 1;

        List<ProjectTask> sorted = allTasks.stream()
                .sorted(Comparator.comparingInt(
                        t -> t.getDisplayOrder() != null ? t.getDisplayOrder() : 0))
                .collect(Collectors.toList());

        List<ProjectTask> children = new ArrayList<>();
        boolean inScope = false;

        for (ProjectTask t : sorted) {
            if (t.getId().equals(parent.getId())) { inScope = true; continue; }
            if (inScope) {
                int tLevel = t.getOutlineLevel() != null ? t.getOutlineLevel() : 1;
                if (tLevel == childLevel)       children.add(t);
                else if (tLevel <= parentLevel) break;
            }
        }
        return children;
    }

    // ══════════════════════════════════════════════════════════════
    // NORMALIZE MILESTONE
    // ══════════════════════════════════════════════════════════════

    private void normalizeMilestone(ProjectTask task) {
        if ("MILESTONE".equalsIgnoreCase(task.getTaskType())) {
            task.setDurationDays(0);
            if (task.getPlannedStart() != null) task.setPlannedEnd(task.getPlannedStart());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // RESOLVE DATES & DURATION
    // ══════════════════════════════════════════════════════════════

    private void resolveDatesAndDuration(ProjectTask task, UpdateProjectTaskRequest req) {
        if ("MILESTONE".equalsIgnoreCase(task.getTaskType())) {
            task.setDurationDays(0);
            if (task.getPlannedStart() != null) task.setPlannedEnd(task.getPlannedStart());
            return;
        }
        LocalDate s = task.getPlannedStart();
        LocalDate e = task.getPlannedEnd();
        Integer d   = req.getDurationDays();

        if (s != null && d != null && d > 0) {
            task.setPlannedEnd(s.plusDays(d - 1));
            task.setDurationDays(d);
        } else if (s != null && e != null) {
            long days = ChronoUnit.DAYS.between(s, e) + 1;
            task.setDurationDays((int) Math.max(1, days));
        } else if (s != null && task.getDurationDays() != null && task.getDurationDays() > 0) {
            task.setPlannedEnd(s.plusDays(task.getDurationDays() - 1));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // MAP TO RESPONSE
    // ══════════════════════════════════════════════════════════════

    public ProjectScheduleTaskResponse mapToResponse(ProjectTask task) {
        List<TaskDependency> deps = taskDependencyRepository
                .findByProjectIdAndSuccessorTaskId(task.getProjectId(), task.getId());

        String predecessorLabel = deps.stream()
                .map(d -> String.valueOf(d.getPredecessorTaskId()))
                .collect(Collectors.joining(", "));

        List<TaskDependencyDto> depDtos = deps.stream()
                .map(this::toDependencyDto).collect(Collectors.toList());

        return new ProjectScheduleTaskResponse()
                .setId(task.getId())
                .setProjectId(task.getProjectId())
                .setParentId(task.getParentId())
                .setWbsCode(task.getWbsCode())
                .setOutlineLevel(task.getOutlineLevel())
                .setName(task.getName())
                .setDescription(task.getDescription())
                .setTaskType(task.getTaskType())
                .setDepartmentCode(task.getDepartmentCode())
                .setResourceType(task.getResourceType())
                .setBaselineStart(task.getBaselineStart())
                .setBaselineEnd(task.getBaselineEnd())
                .setPlannedStart(task.getPlannedStart())
                .setPlannedEnd(task.getPlannedEnd())
                .setActualStart(task.getActualStart())
                .setActualEnd(task.getActualEnd())
                .setDurationDays(task.getDurationDays())
                .setPercentComplete(task.getPercentComplete())
                .setAllocationPercent(task.getAllocationPercent())
                .setPriority(task.getPriority())
                .setScheduleMode(task.getScheduleMode())
                .setStatus(task.getStatus())
                .setColor(task.getColor())
                .setActive(task.getActive())
                .setDisplayOrder(task.getDisplayOrder())
                .setCustomerMilestone(task.getCustomerMilestone())
                .setAssignedUserId(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null)
                .setAssignedUserName(task.getAssignedUser() != null ? task.getAssignedUser().getFullName() : null)
                .setDependencies(depDtos)
                .setPredecessorLabel(predecessorLabel);
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private TaskDependencyDto toDependencyDto(TaskDependency dep) {
        TaskDependencyDto dto = new TaskDependencyDto();
        dto.setId(dep.getId());
        dto.setPredecessorTaskId(dep.getPredecessorTaskId());
        dto.setSuccessorTaskId(dep.getSuccessorTaskId());
        dto.setDependencyType(dep.getDependencyType());
        dto.setLagDays(dep.getLagDays());
        return dto;
    }

    private void validateDates(UpdateProjectTaskRequest req) {
        if (req.getPlannedStart() != null && req.getPlannedEnd() != null
                && req.getPlannedEnd().isBefore(req.getPlannedStart()))
            throw new IllegalArgumentException("Planned end cannot be before planned start.");
        if (req.getBaselineStart() != null && req.getBaselineEnd() != null
                && req.getBaselineEnd().isBefore(req.getBaselineStart()))
            throw new IllegalArgumentException("Baseline end cannot be before baseline start.");
        if (req.getActualStart() != null && req.getActualEnd() != null
                && req.getActualEnd().isBefore(req.getActualStart()))
            throw new IllegalArgumentException("Actual end cannot be before actual start.");
    }

    private void validatePercent(Integer value, String label) {
        if (value != null && (value < 0 || value > 100))
            throw new IllegalArgumentException(label + " must be between 0 and 100.");
    }

    private ProjectTask copyForHistory(ProjectTask source) {
    ProjectTask copy = new ProjectTask();

    copy.setName(source.getName());
    copy.setPlannedStart(source.getPlannedStart());
    copy.setPlannedEnd(source.getPlannedEnd());
    copy.setBaselineStart(source.getBaselineStart());
    copy.setBaselineEnd(source.getBaselineEnd());
    copy.setActualStart(source.getActualStart());
    copy.setActualEnd(source.getActualEnd());
    copy.setDurationDays(source.getDurationDays());
    copy.setPercentComplete(source.getPercentComplete());
    copy.setDepartmentCode(source.getDepartmentCode());
    copy.setResourceType(source.getResourceType());
    copy.setCustomerMilestone(source.getCustomerMilestone());

    return copy;
    }

    private Long getOrganisationIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getDetails() instanceof Map<?, ?> details) {
            Object value = details.get("organisationId");
            return value == null ? null : Long.valueOf(value.toString());
        }

        return null;
    }

    private Long getUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getDetails() instanceof Map<?, ?> details) {
            Object value = details.get("userId");
            return value == null ? null : Long.valueOf(value.toString());
        }

        return null;
    }

    private String getUsernameFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "System";
    }
}