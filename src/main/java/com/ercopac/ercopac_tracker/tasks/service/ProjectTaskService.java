package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.department.dto.DepartmentDto;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
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
import com.ercopac.ercopac_tracker.user.ResourceTypeDto;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final TaskConsoleService               taskConsoleService;
    private final ResourceTypeRepository           resourceTypeRepository;
    private final DepartmentRepository             departmentRepository;

    public ProjectTaskService(
            ProjectTaskRepository projectTaskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TaskDependencyRepository taskDependencyRepository,
            TaskResourceAssignmentRepository taskResourceAssignmentRepository,
            TaskSchedulingService taskSchedulingService,
            ProjectTaskHistoryService historyService,
            TaskConsoleService taskConsoleService,
            ResourceTypeRepository resourceTypeRepository,
            DepartmentRepository departmentRepository) {
        this.projectTaskRepository            = projectTaskRepository;
        this.projectRepository                = projectRepository;
        this.userRepository                   = userRepository;
        this.taskDependencyRepository         = taskDependencyRepository;
        this.taskResourceAssignmentRepository = taskResourceAssignmentRepository;
        this.taskSchedulingService            = taskSchedulingService;
        this.historyService                   = historyService;
        this.taskConsoleService               = taskConsoleService;
        this.resourceTypeRepository           = resourceTypeRepository;
        this.departmentRepository             = departmentRepository;
    }

    // ══════════════════════════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════════════════════════
    public ProjectScheduleTaskResponse updateTask(Long taskId, UpdateProjectTaskRequest request) {
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        ProjectTask oldTask = copyForHistory(task);

        validateDates(request);
        validatePercent(request.getPercentComplete(), "percentComplete");

        Integer oldPercent = task.getPercentComplete() != null ? task.getPercentComplete() : 0;

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
        task.setActive(request.getActive());
        task.setDisplayOrder(request.getDisplayOrder());
        task.setOutlineLevel(request.getOutlineLevel());
        task.setCustomerMilestone(request.getCustomerMilestone());
        task.setScheduleMode(request.getScheduleMode());
        task.setStatus(request.getStatus());
        task.setColor(request.getColor());

        // parentId set directly from request
        if (request.getParentId() != null) {
            task.setParentId(request.getParentId());
        } else {
            task.setParentId(null);
        }

        // Assigned user — sync department from user
        if (request.getAssignedUserId() != null) {
            userRepository.findById(request.getAssignedUserId()).ifPresent(user -> {
                task.setAssignedUser(user);
                if (user.getDepartment() != null) {
                    task.setDepartment(user.getDepartment());
                } else if (user.getDepartmentCode() != null) {
                    task.setDepartmentCode(user.getDepartmentCode());
                }
            });
        } else {
            task.setAssignedUser(null);
        }

        // Resolve resourceType FK from code string sent by frontend
        if (request.getResourceType() != null && !request.getResourceType().isBlank()) {
            Long orgId = getOrganisationIdFromSecurityContext();
            if (orgId != null) {
                resourceTypeRepository.findByCodeAndOrganisationId(
                    request.getResourceType(), orgId)
                    .ifPresent(task::setResourceType);
            } else {
                task.setResourceTypeCode(request.getResourceType());
            }
        } else {
            task.setResourceType(null);
            task.setResourceTypeCode(null);
        }

        // Resolve department FK from code string sent by frontend
        if (task.getDepartment() == null &&
            request.getDepartmentCode() != null &&
            !request.getDepartmentCode().isBlank()) {
            Long orgId = getOrganisationIdFromSecurityContext();
            if (orgId != null) {
                departmentRepository.findByCodeAndOrganisationId(
                    request.getDepartmentCode(), orgId)
                    .ifPresent(task::setDepartment);
            } else {
                task.setDepartmentCode(request.getDepartmentCode());
            }
        }

        resolveDatesAndDuration(task, request, oldTask);
        normalizeMilestone(task);

        Long projectId = task.getProjectId();

        historyService.logTaskUpdate(
                oldTask, task,
                getOrganisationIdFromSecurityContext(),
                getUserIdFromSecurityContext(),
                getUsernameFromSecurityContext()
        );

        ProjectTask saved = projectTaskRepository.save(task);
        Integer newPercent = saved.getPercentComplete() != null ? saved.getPercentComplete() : 0;

        rollupSummaries(projectId);
        taskSchedulingService.rescheduleFromTask(projectId, saved.getId());

        ProjectTask finalTask = projectTaskRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + saved.getId()));

        try {
            taskConsoleService.checkProgressNotifications(
                    finalTask, oldPercent, newPercent,
                    getOrganisationIdFromSecurityContext(),
                    getUserIdFromSecurityContext(),
                    getUsernameFromSecurityContext()
            );
        } catch (Exception e) {
            System.out.println("NOTIFICATION ERROR, TASK SAVE CONTINUES: " + e.getMessage());
        }

        return mapToResponse(finalTask);
    }

    // ══════════════════════════════════════════════════════════════
    // RESOURCE TYPES & DEPARTMENTS
    // ══════════════════════════════════════════════════════════════
    public List<ResourceTypeDto> getResourceTypesForProject(Long projectId) {
        Long orgId = getOrgIdForProject(projectId);
        if (orgId == null) return List.of();
        return resourceTypeRepository
            .findByOrganisationIdAndActiveTrueOrderByCodeAsc(orgId)
            .stream()
            .map(rt -> new ResourceTypeDto(rt.getId(), rt.getCode(), rt.getLabel(), rt.getColour()))
            .collect(Collectors.toList());
    }

    public List<DepartmentDto> getDepartmentsForProject(Long projectId) {
        Long orgId = getOrgIdForProject(projectId);
        if (orgId == null) return List.of();
        return departmentRepository
            .findByOrganisationIdOrderByCodeAsc(orgId)
            .stream()
            .map(d -> new DepartmentDto(d.getId(), d.getCode(), d.getLabel()))
            .collect(Collectors.toList());
    }

    private Long getOrgIdForProject(Long projectId) {
        return projectRepository.findById(projectId)
            .map(p -> p.getOrganisation() != null ? p.getOrganisation().getId() : null)
            .orElse(null);
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
        task.setPlannedEnd(request.getPlannedEnd() != null ? request.getPlannedEnd() : request.getPlannedStart());
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
        Integer newPercent = saved.getPercentComplete() != null ? saved.getPercentComplete() : 0;

        taskConsoleService.checkProgressNotifications(
                saved, 0, newPercent,
                getOrganisationIdFromSecurityContext(),
                getUserIdFromSecurityContext(),
                getUsernameFromSecurityContext()
        );

        rebuildStructureFromParentId(projectId);
        rollupSummaries(projectId);

        return mapToResponse(projectTaskRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found after insert: " + saved.getId())));
    }

    // ══════════════════════════════════════════════════════════════
    // COPY BELOW
    // ══════════════════════════════════════════════════════════════
    public ProjectScheduleTaskResponse copyTaskBelow(Long projectId, Long sourceTaskId) {
        List<ProjectTask> allTasks = projectTaskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);

        ProjectTask source = allTasks.stream()
                .filter(t -> sourceTaskId.equals(t.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + sourceTaskId));

        int sourceLevel        = source.getOutlineLevel() != null ? source.getOutlineLevel() : 1;
        int sourceDisplayOrder = source.getDisplayOrder() != null ? source.getDisplayOrder() : 0;

        List<ProjectTask> subtree = new ArrayList<>();
        for (ProjectTask t : allTasks) {
            int tOrder = t.getDisplayOrder() != null ? t.getDisplayOrder() : 0;
            int tLevel = t.getOutlineLevel() != null ? t.getOutlineLevel() : 1;
            if (tOrder < sourceDisplayOrder) continue;
            if (tOrder == sourceDisplayOrder) { subtree.add(t); continue; }
            if (tLevel > sourceLevel) subtree.add(t);
            else break;
        }

        int subtreeSize = subtree.size();
        int lastOrder = subtree.stream()
                .mapToInt(t -> t.getDisplayOrder() != null ? t.getDisplayOrder() : 0)
                .max().orElse(sourceDisplayOrder);

        for (ProjectTask t : allTasks) {
            int tOrder = t.getDisplayOrder() != null ? t.getDisplayOrder() : 0;
            boolean inSubtree = subtree.stream().anyMatch(s -> s.getId().equals(t.getId()));
            if (!inSubtree && tOrder > lastOrder) {
                t.setDisplayOrder(tOrder + subtreeSize);
                projectTaskRepository.save(t);
            }
        }

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
            // FIX: copy FK entity + string code separately
            copy.setResourceType(orig.getResourceType());
            copy.setResourceTypeCode(orig.getResourceTypeCode());
            copy.setDepartment(orig.getDepartment());
            copy.setDepartmentCode(orig.getDepartmentCode());
            copy.setScheduleMode(orig.getScheduleMode() != null ? orig.getScheduleMode() : "AUTO");
            copy.setActive(orig.getActive());
            copy.setDisplayOrder(lastOrder + 1 + i);
            copy.setCustomerMilestone(orig.getCustomerMilestone());
            copy.setAssignedUser(orig.getAssignedUser());
            copy.setAllocationPercent(orig.getAllocationPercent());
            copy.setStatus(orig.getStatus());
            copy.setColor(orig.getColor());
            copy.setOutlineLevel(orig.getOutlineLevel());

            if (i == 0) {
                copy.setParentId(source.getParentId());
            } else {
                int myLevel = orig.getOutlineLevel() != null ? orig.getOutlineLevel() : 1;
                int parentLevel = myLevel - 1;
                ProjectTask parentCopy = null;
                for (int j = i - 1; j >= 0; j--) {
                    int jLevel = subtree.get(j).getOutlineLevel() != null ? subtree.get(j).getOutlineLevel() : 1;
                    if (jLevel == parentLevel) { parentCopy = savedList.get(j); break; }
                }
                copy.setParentId(parentCopy != null ? parentCopy.getId() : source.getParentId());
            }

            normalizeMilestone(copy);
            savedList.add(projectTaskRepository.save(copy));
        }

        rebuildStructureFromParentId(projectId);
        rollupSummaries(projectId);

        return mapToResponse(projectTaskRepository.findById(savedList.get(0).getId())
                .orElseThrow(() -> new IllegalArgumentException("Root copy not found")));
    }

    // ══════════════════════════════════════════════════════════════
    // DELETE
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
    // ROLLUP SUMMARIES
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

            tasks = projectTaskRepository
                    .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);
            List<ProjectTask> summaries = tasks.stream()
                    .filter(t -> "SUMMARY".equalsIgnoreCase(t.getTaskType()))
                    .filter(t -> Objects.equals(
                            t.getOutlineLevel() != null ? t.getOutlineLevel() : 1, currentLevel))
                    .collect(Collectors.toList());

            for (ProjectTask summary : summaries) {
                List<ProjectTask> children = getDirectChildren(summary, tasks);
                if (children.isEmpty()) continue;

                Optional<LocalDate> minPlannedStart = children.stream()
                        .map(ProjectTask::getPlannedStart).filter(Objects::nonNull)
                        .min(Comparator.naturalOrder());
                Optional<LocalDate> maxPlannedEnd = children.stream()
                        .map(ProjectTask::getPlannedEnd).filter(Objects::nonNull)
                        .max(Comparator.naturalOrder());
                Optional<LocalDate> minBaselineStart = children.stream()
                        .map(ProjectTask::getBaselineStart).filter(Objects::nonNull)
                        .min(Comparator.naturalOrder());
                Optional<LocalDate> maxBaselineEnd = children.stream()
                        .map(ProjectTask::getBaselineEnd).filter(Objects::nonNull)
                        .max(Comparator.naturalOrder());
                Optional<LocalDate> minActualStart = children.stream()
                        .map(ProjectTask::getActualStart).filter(Objects::nonNull)
                        .min(Comparator.naturalOrder());
                Optional<LocalDate> maxActualEnd = children.stream()
                        .map(ProjectTask::getActualEnd).filter(Objects::nonNull)
                        .max(Comparator.naturalOrder());

                int totalDuration = children.stream()
                        .mapToInt(c -> c.getDurationDays() != null ? c.getDurationDays() : 0)
                        .sum();

                long totalWeight = children.stream()
                        .mapToLong(c -> c.getDurationDays() != null && c.getDurationDays() > 0
                                ? c.getDurationDays() : 1)
                        .sum();
                double weightedPct = children.stream()
                        .mapToDouble(c -> {
                            int dur = c.getDurationDays() != null && c.getDurationDays() > 0
                                    ? c.getDurationDays() : 1;
                            int pct = c.getPercentComplete() != null ? c.getPercentComplete() : 0;
                            return (double) dur * pct;
                        }).sum();
                int avgPct = totalWeight > 0 ? (int) Math.round(weightedPct / totalWeight) : 0;

                minPlannedStart.ifPresent(summary::setPlannedStart);
                maxPlannedEnd.ifPresent(summary::setPlannedEnd);
                minBaselineStart.ifPresent(summary::setBaselineStart);
                maxBaselineEnd.ifPresent(summary::setBaselineEnd);
                minActualStart.ifPresent(summary::setActualStart);
                maxActualEnd.ifPresent(summary::setActualEnd);
                summary.setDurationDays(totalDuration);
                summary.setPercentComplete(avgPct);
                projectTaskRepository.save(summary);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // REBUILD STRUCTURE
    // ══════════════════════════════════════════════════════════════
    private void rebuildStructureFromParentId(Long projectId) {
        List<ProjectTask> tasks = projectTaskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);
        if (tasks.isEmpty()) return;

        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setDisplayOrder(i + 1);
        }

        Map<Long, List<ProjectTask>> childrenMap = new LinkedHashMap<>();
        List<ProjectTask> roots = new ArrayList<>();

        for (ProjectTask task : tasks) {
            if (task.getParentId() == null) {
                roots.add(task);
            } else {
                childrenMap
                    .computeIfAbsent(task.getParentId(), k -> new ArrayList<>())
                    .add(task);
            }
        }

        assignWbsCodes(roots, childrenMap, "", 1);

        for (ProjectTask task : tasks) {
            projectTaskRepository.save(task);
        }
    }

    private void assignWbsCodes(
            List<ProjectTask> tasks,
            Map<Long, List<ProjectTask>> childrenMap,
            String prefix,
            int level) {

        for (int i = 0; i < tasks.size(); i++) {
            ProjectTask task = tasks.get(i);
            String wbs = prefix.isEmpty()
                ? String.valueOf(i + 1)
                : prefix + "." + (i + 1);

            task.setWbsCode(wbs);
            task.setOutlineLevel(level);

            List<ProjectTask> children = childrenMap.get(task.getId());
            if (children != null && !children.isEmpty()) {
                assignWbsCodes(children, childrenMap, wbs, level + 1);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // COLLECT SUBTREE
    // ══════════════════════════════════════════════════════════════
    private List<ProjectTask> collectSubtree(ProjectTask root, List<ProjectTask> allTasks) {
        List<ProjectTask> result = new ArrayList<>();
        result.add(root);
        int rootLevel = root.getOutlineLevel() != null ? root.getOutlineLevel() : 1;
        List<ProjectTask> sorted = allTasks.stream()
                .sorted(Comparator.comparingInt(t -> t.getDisplayOrder() != null ? t.getDisplayOrder() : 0))
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
        List<ProjectTask> byParentId = allTasks.stream()
                .filter(t -> parent.getId().equals(t.getParentId()))
                .collect(Collectors.toList());
        if (!byParentId.isEmpty()) return byParentId;

        int parentLevel = parent.getOutlineLevel() != null ? parent.getOutlineLevel() : 1;
        int childLevel = parentLevel + 1;
        List<ProjectTask> sorted = allTasks.stream()
                .sorted(Comparator.comparingInt(t -> t.getDisplayOrder() != null ? t.getDisplayOrder() : 0))
                .collect(Collectors.toList());
        List<ProjectTask> children = new ArrayList<>();
        boolean inScope = false;
        for (ProjectTask t : sorted) {
            if (t.getId().equals(parent.getId())) { inScope = true; continue; }
            if (inScope) {
                int tLevel = t.getOutlineLevel() != null ? t.getOutlineLevel() : 1;
                if (tLevel == childLevel) children.add(t);
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
            if (task.getBaselineStart() != null) task.setBaselineEnd(task.getBaselineStart());
            if (task.getActualStart() != null) task.setActualEnd(task.getActualStart());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // RESOLVE DATES & DURATION
    // ══════════════════════════════════════════════════════════════
    // FIX: bidirectional relationship between durationDays and actual dates.
    //   - If the user changed Duration        → recompute actualEnd = actualStart + duration.
    //   - If the user changed Actual Start/End → recompute durationDays = span(actualStart, actualEnd).
    // We detect "which one the user actually changed" by comparing against
    // oldTask (the entity snapshot taken before any request values were applied),
    // not against `task`'s current values (which have already been overwritten
    // with the request's values earlier in updateTask()).
    private void resolveDatesAndDuration(ProjectTask task, UpdateProjectTaskRequest req, ProjectTask oldTask) {
        String type = (task.getTaskType() != null ? task.getTaskType() : "ACTIVITY").toUpperCase();

        if ("MILESTONE".equals(type)) {
            task.setDurationDays(0);
            if (task.getPlannedStart() != null)  task.setPlannedEnd(task.getPlannedStart());
            if (task.getBaselineStart() != null) task.setBaselineEnd(task.getBaselineStart());
            if (task.getActualStart() != null)   task.setActualEnd(task.getActualStart());
            return;
        }

        if ("SUMMARY".equals(type)) return;

        LocalDate plannedStart    = task.getPlannedStart();
        LocalDate baselineStart   = task.getBaselineStart();
        Integer previousDuration  = oldTask.getDurationDays();
        Integer requestedDuration = req.getDurationDays();

        LocalDate previousActualStart = oldTask.getActualStart();
        LocalDate previousActualEnd   = oldTask.getActualEnd();
        LocalDate newActualStart = task.getActualStart(); // already overwritten with request value
        LocalDate newActualEnd   = task.getActualEnd();   // already overwritten with request value

        boolean durationChanged = requestedDuration != null && requestedDuration > 0
                && (previousDuration == null || !previousDuration.equals(requestedDuration));

        boolean actualStartChanged = !Objects.equals(previousActualStart, newActualStart);
        boolean actualEndChanged   = !Objects.equals(previousActualEnd, newActualEnd);

        if (requestedDuration != null && requestedDuration > 0) {
            task.setDurationDays(requestedDuration);

            if (durationChanged) {
                // Duration → ActualEnd
                if (newActualStart != null) {
                    task.setActualEnd(newActualStart.plusDays(requestedDuration - 1));
                }
            } else if ((actualStartChanged || actualEndChanged) && newActualStart != null && newActualEnd != null) {
                // Reverse: ActualStart/ActualEnd → Duration
                long days = ChronoUnit.DAYS.between(newActualStart, newActualEnd) + 1;
                task.setDurationDays((int) Math.max(1, days));
            }
            return;
        }

        if (plannedStart != null && task.getPlannedEnd() != null) {
            long days = ChronoUnit.DAYS.between(plannedStart, task.getPlannedEnd()) + 1;
            task.setDurationDays((int) Math.max(1, days));
            return;
        }

        if (baselineStart != null && task.getBaselineEnd() != null) {
            long days = ChronoUnit.DAYS.between(baselineStart, task.getBaselineEnd()) + 1;
            task.setDurationDays((int) Math.max(1, days));
            if (plannedStart == null) task.setPlannedStart(baselineStart);
            if (task.getPlannedEnd() == null) task.setPlannedEnd(task.getBaselineEnd());
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

        // FIX: use getResourceTypeCode() — returns String code from FK or fallback string
        String resourceTypeCode = task.getResourceTypeCode() != null && !task.getResourceTypeCode().isBlank()
                ? task.getResourceTypeCode()
                : (task.getAssignedUser() != null && task.getAssignedUser().getResourceType() != null
                    ? task.getAssignedUser().getResourceType().getCode() : null);

        // FIX: use getDepartmentCode() — returns String code from FK or fallback string
        String departmentCode = task.getDepartmentCode() != null && !task.getDepartmentCode().isBlank()
                ? task.getDepartmentCode()
                : (task.getAssignedUser() != null
                    ? task.getAssignedUser().getDepartmentCode() : null);

        return new ProjectScheduleTaskResponse()
                .setId(task.getId())
                .setProjectId(task.getProjectId())
                .setParentId(task.getParentId())
                .setWbsCode(task.getWbsCode())
                .setOutlineLevel(task.getOutlineLevel())
                .setName(task.getName())
                .setDescription(task.getDescription())
                .setTaskType(task.getTaskType())
                .setDepartmentCode(departmentCode)
                .setResourceType(resourceTypeCode)
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
    // IMPORT SCHEDULE
    // ══════════════════════════════════════════════════════════════
    public List<ProjectScheduleTaskResponse> importSchedule(
            Long projectId,
            List<UpdateProjectTaskRequest> importedTasks) {

        if (importedTasks == null || importedTasks.isEmpty()) return List.of();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        Long organisationId = project.getOrganisation() != null
                ? project.getOrganisation().getId() : null;
        if (organisationId == null) throw new IllegalStateException("Project has no organisation");

        List<ProjectTask> oldTasks =
                projectTaskRepository.findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);
        for (ProjectTask task : oldTasks) {
            taskDependencyRepository.deleteByPredecessorTaskId(task.getId());
            taskDependencyRepository.deleteBySuccessorTaskId(task.getId());
            taskResourceAssignmentRepository.deleteByProjectIdAndTaskId(projectId, task.getId());
            projectTaskRepository.delete(task);
        }
        projectTaskRepository.flush();

        List<ProjectTask> savedTasks = new ArrayList<>();
        for (int i = 0; i < importedTasks.size(); i++) {
            UpdateProjectTaskRequest req = importedTasks.get(i);
            ProjectTask task = new ProjectTask();
            task.setProjectId(projectId);
            task.setOrganisationId(organisationId);
            task.setName(req.getName() != null && !req.getName().isBlank()
                    ? req.getName() : "Imported Task " + (i + 1));
            task.setDescription(req.getDescription());
            task.setTaskType(req.getTaskType() != null ? req.getTaskType() : "ACTIVITY");
            task.setBaselineStart(req.getBaselineStart() != null ? req.getBaselineStart() : req.getPlannedStart());
            task.setBaselineEnd(req.getBaselineEnd() != null ? req.getBaselineEnd() : req.getPlannedEnd());
            task.setPlannedStart(req.getPlannedStart() != null ? req.getPlannedStart() : task.getBaselineStart());
            task.setPlannedEnd(req.getPlannedEnd() != null ? req.getPlannedEnd() : task.getBaselineEnd());
            if (task.getBaselineStart() != null && task.getBaselineEnd() == null)
                task.setBaselineEnd(task.getBaselineStart());
            if (task.getPlannedStart() != null && task.getPlannedEnd() == null)
                task.setPlannedEnd(task.getPlannedStart());
            task.setActualStart(req.getActualStart());
            task.setActualEnd(req.getActualEnd());
            task.setDurationDays(req.getDurationDays() != null
                    ? req.getDurationDays() : calculateImportedDuration(task));
            task.setPercentComplete(req.getPercentComplete() != null ? req.getPercentComplete() : 0);
            task.setAllocationPercent(req.getAllocationPercent() != null ? req.getAllocationPercent() : 100);
            task.setPriority(req.getPriority() != null ? req.getPriority() : 500);
            task.setWbsCode(req.getWbsCode());
            task.setDepartmentCode(req.getDepartmentCode());
            // FIX: use setResourceTypeCode for String from request
            task.setResourceTypeCode(req.getResourceType());
            task.setActive(req.getActive() != null ? req.getActive() : true);
            task.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : i + 1);
            task.setOutlineLevel(req.getOutlineLevel() != null ? req.getOutlineLevel() : 1);
            task.setCustomerMilestone(req.getCustomerMilestone() != null ? req.getCustomerMilestone() : false);
            task.setScheduleMode(req.getScheduleMode() != null ? req.getScheduleMode() : "AUTO");
            task.setStatus(req.getStatus() != null ? req.getStatus() : "NOT_STARTED");
            task.setColor(req.getColor());
            normalizeMilestone(task);
            savedTasks.add(projectTaskRepository.save(task));
        }

        rebuildStructureFromParentId(projectId);
        rollupSummaries(projectId);

        return projectTaskRepository
                .findByProjectIdOrderByDisplayOrderAscIdAsc(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Integer calculateImportedDuration(ProjectTask task) {
        if ("MILESTONE".equalsIgnoreCase(task.getTaskType())) return 0;
        if (task.getBaselineStart() == null || task.getBaselineEnd() == null) return 1;
        long days = ChronoUnit.DAYS.between(task.getBaselineStart(), task.getBaselineEnd()) + 1;
        return Math.max(1, (int) days);
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
        // FIX: copy both FK entity and string code
        copy.setResourceType(source.getResourceType());
        copy.setResourceTypeCode(source.getResourceTypeCode());
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