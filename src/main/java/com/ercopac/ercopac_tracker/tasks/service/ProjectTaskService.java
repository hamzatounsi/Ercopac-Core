package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.dto.CreateTaskRequest;
import com.ercopac.ercopac_tracker.tasks.dto.ProjectScheduleTaskResponse;
import com.ercopac.ercopac_tracker.tasks.dto.TaskDependencyDto;
import com.ercopac.ercopac_tracker.tasks.dto.UpdateProjectTaskRequest;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskResourceAssignmentRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectTaskService {

    private final ProjectTaskRepository projectTaskRepository;
    private final UserRepository userRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskResourceAssignmentRepository taskResourceAssignmentRepository;
    private final TaskSchedulingService taskSchedulingService;

    public ProjectTaskService(
            ProjectTaskRepository projectTaskRepository,
            UserRepository userRepository,
            TaskDependencyRepository taskDependencyRepository,
            TaskResourceAssignmentRepository taskResourceAssignmentRepository,
            TaskSchedulingService taskSchedulingService) {
        this.projectTaskRepository = projectTaskRepository;
        this.userRepository = userRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.taskResourceAssignmentRepository = taskResourceAssignmentRepository;
        this.taskSchedulingService = taskSchedulingService;
    }

    public ProjectScheduleTaskResponse updateTask(Long taskId, UpdateProjectTaskRequest request) {
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        validateDates(request);
        validatePercent(request.getPercentComplete(), "Percent complete");
        validatePercent(request.getAllocationPercent(), "Allocation percent");

        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setBaselineStart(request.getBaselineStart());
        task.setBaselineEnd(request.getBaselineEnd());
        task.setPlannedStart(request.getPlannedStart());
        task.setPlannedEnd(request.getPlannedEnd());
        task.setActualStart(request.getActualStart());
        task.setActualEnd(request.getActualEnd());
        task.setPercentComplete(request.getPercentComplete());
        task.setAllocationPercent(request.getAllocationPercent());
        task.setPriority(request.getPriority());
        task.setTaskType(request.getTaskType());
        task.setDepartmentCode(request.getDepartmentCode());
        task.setResourceType(request.getResourceType());
        task.setActive(request.getActive());
        task.setCustomerMilestone(request.getCustomerMilestone());
        task.setScheduleMode(request.getScheduleMode());
        task.setStatus(request.getStatus());
        task.setColor(request.getColor());

        if (request.getDisplayOrder() != null) {
            task.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getOutlineLevel() != null) {
            task.setOutlineLevel(request.getOutlineLevel());
        }

        if (request.getAssignedUserId() != null) {
            AppUser assignedUser = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Assigned user not found with id: " + request.getAssignedUserId()));
            task.setAssignedUser(assignedUser);
        } else {
            task.setAssignedUser(null);
        }

        if (request.getDurationDays() != null) {
            task.setDurationDays(request.getDurationDays());
        } else if (request.getPlannedStart() != null && request.getPlannedEnd() != null) {
            int duration = (int) ChronoUnit.DAYS.between(request.getPlannedStart(), request.getPlannedEnd()) + 1;
            task.setDurationDays(Math.max(duration, 1));
        }

        normalizeMilestone(task);

        ProjectTask saved = projectTaskRepository.save(task);

        rebuildStructure(saved.getProjectId());

        taskSchedulingService.rescheduleFromTask(saved.getProjectId(), saved.getId());

        ProjectTask refreshed = projectTaskRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found after save: " + saved.getId()));

        return mapToResponse(refreshed);
    }

    public ProjectScheduleTaskResponse createTaskBelow(Long projectId, Long afterTaskId, CreateTaskRequest request) {
        ProjectTask anchor = projectTaskRepository.findById(afterTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Anchor task not found with id: " + afterTaskId));

        if (!Objects.equals(anchor.getProjectId(), projectId)) {
            throw new IllegalArgumentException("Anchor task does not belong to project " + projectId);
        }

        Integer anchorOrder = anchor.getDisplayOrder() != null ? anchor.getDisplayOrder() : 0;

        shiftDisplayOrdersFrom(projectId, anchorOrder + 1);

        ProjectTask task = new ProjectTask();
        task.setProjectId(projectId);
        task.setName(request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : "New Task");
        task.setDescription(request.getDescription());
        task.setDurationDays(request.getDurationDays() != null ? request.getDurationDays() : 1);
        task.setPlannedStart(request.getPlannedStart());
        task.setPlannedEnd(request.getPlannedEnd());
        task.setPercentComplete(request.getPercentComplete() != null ? request.getPercentComplete() : 0);
        task.setPriority(request.getPriority() != null ? request.getPriority() : 500);
        task.setScheduleMode(request.getScheduleMode() != null ? request.getScheduleMode() : "AUTO");
        task.setActive(request.getActive() != null ? request.getActive() : true);
        task.setDisplayOrder(anchorOrder + 1);
        task.setOutlineLevel(anchor.getOutlineLevel() != null ? anchor.getOutlineLevel() : 1);

        task.setTaskType(anchor.getTaskType() != null ? anchor.getTaskType() : "ACTIVITY");
        task.setDepartmentCode(anchor.getDepartmentCode());
        task.setResourceType(anchor.getResourceType());
        task.setCustomerMilestone(false);

        if ("MILESTONE".equalsIgnoreCase(task.getTaskType())) {
            task.setPlannedEnd(task.getPlannedStart());
            task.setDurationDays(0);
        } else if (task.getPlannedStart() != null && task.getPlannedEnd() == null) {
            task.setPlannedEnd(task.getPlannedStart().plusDays(Math.max(0, task.getDurationDays() - 1)));
        } else if (task.getPlannedStart() != null && task.getPlannedEnd() != null) {
            int duration = (int) ChronoUnit.DAYS.between(task.getPlannedStart(), task.getPlannedEnd()) + 1;
            task.setDurationDays(Math.max(duration, 1));
        }

        ProjectTask saved = projectTaskRepository.save(task);

        rebuildStructure(projectId);

        ProjectTask refreshed = projectTaskRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found after create: " + saved.getId()));

        return mapToResponse(refreshed);
    }

    public ProjectScheduleTaskResponse copyTaskBelow(Long projectId, Long sourceTaskId) {
        ProjectTask source = projectTaskRepository.findById(sourceTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Source task not found with id: " + sourceTaskId));

        if (!Objects.equals(source.getProjectId(), projectId)) {
            throw new IllegalArgumentException("Source task does not belong to project " + projectId);
        }

        Integer sourceOrder = source.getDisplayOrder() != null ? source.getDisplayOrder() : 0;

        shiftDisplayOrdersFrom(projectId, sourceOrder + 1);

        ProjectTask copy = new ProjectTask();
        copy.setProjectId(projectId);
        copy.setName((source.getName() != null ? source.getName() : "Task") + " Copy");
        copy.setDescription(source.getDescription());
        copy.setDurationDays(source.getDurationDays());
        copy.setPlannedStart(source.getPlannedStart());
        copy.setPlannedEnd(source.getPlannedEnd());
        copy.setBaselineStart(source.getBaselineStart());
        copy.setBaselineEnd(source.getBaselineEnd());
        copy.setActualStart(source.getActualStart());
        copy.setActualEnd(source.getActualEnd());
        copy.setPercentComplete(source.getPercentComplete());
        copy.setAllocationPercent(source.getAllocationPercent());
        copy.setPlannedHours(source.getPlannedHours());
        copy.setActualHours(source.getActualHours());
        copy.setPriority(source.getPriority());
        copy.setScheduleMode(source.getScheduleMode());
        copy.setStatus(source.getStatus());
        copy.setColor(source.getColor());
        copy.setActive(source.getActive());
        copy.setDisplayOrder(sourceOrder + 1);
        copy.setOutlineLevel(source.getOutlineLevel() != null ? source.getOutlineLevel() : 1);
        copy.setTaskType(source.getTaskType());
        copy.setDepartmentCode(source.getDepartmentCode());
        copy.setCustomerMilestone(source.getCustomerMilestone());
        copy.setAssignedUser(source.getAssignedUser());
        copy.setResourceType(source.getResourceType());

        normalizeMilestone(copy);

        ProjectTask saved = projectTaskRepository.save(copy);

        rebuildStructure(projectId);

        ProjectTask refreshed = projectTaskRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found after copy: " + saved.getId()));

        return mapToResponse(refreshed);
    }

    public void deleteTask(Long taskId) {
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        Long projectId = task.getProjectId();

        taskDependencyRepository.deleteByPredecessorTaskId(taskId);
        taskDependencyRepository.deleteBySuccessorTaskId(taskId);
        taskResourceAssignmentRepository.deleteByProjectIdAndTaskId(projectId, taskId);
        projectTaskRepository.delete(task);

        rebuildStructure(projectId);
    }

    private void shiftDisplayOrdersFrom(Long projectId, int startOrder) {
        List<ProjectTask> tasks = projectTaskRepository.findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);

        for (ProjectTask task : tasks) {
            int currentOrder = task.getDisplayOrder() != null ? task.getDisplayOrder() : 0;
            if (currentOrder >= startOrder) {
                task.setDisplayOrder(currentOrder + 1);
            }
        }

        projectTaskRepository.saveAll(tasks);
    }

    private void normalizeMilestone(ProjectTask task) {
        if ("MILESTONE".equalsIgnoreCase(task.getTaskType())) {
            if (task.getPlannedStart() != null) {
                task.setPlannedEnd(task.getPlannedStart());
            }
            if (task.getBaselineStart() != null) {
                task.setBaselineEnd(task.getBaselineStart());
            }
            if (task.getActualStart() != null) {
                task.setActualEnd(task.getActualStart());
            }
            task.setDurationDays(0);
        }
    }

    private ProjectScheduleTaskResponse mapToResponse(ProjectTask task) {
        List<TaskDependencyDto> dependencyDtos = taskDependencyRepository.findBySuccessorTaskId(task.getId()).stream()
                .map(this::toDependencyDto)
                .collect(Collectors.toList());

        String predecessorLabel = dependencyDtos.stream()
                .map(dep -> dep.getPredecessorTaskId() + (dep.getDependencyType() != null ? dep.getDependencyType() : "FS"))
                .collect(Collectors.joining(", "));

        return new ProjectScheduleTaskResponse()
                .setId(task.getId())
                .setProjectId(task.getProjectId())
                .setName(task.getName())
                .setDescription(task.getDescription())
                .setDurationDays(task.getDurationDays())
                .setBaselineStart(task.getBaselineStart())
                .setBaselineEnd(task.getBaselineEnd())
                .setPlannedStart(task.getPlannedStart())
                .setPlannedEnd(task.getPlannedEnd())
                .setActualStart(task.getActualStart())
                .setActualEnd(task.getActualEnd())
                .setPercentComplete(task.getPercentComplete())
                .setAllocationPercent(task.getAllocationPercent())
                .setPlannedHours(task.getPlannedHours())
                .setActualHours(task.getActualHours())
                .setPriority(task.getPriority())
                .setScheduleMode(task.getScheduleMode())
                .setStatus(task.getStatus())
                .setColor(task.getColor())
                .setTaskType(task.getTaskType())
                .setWbsCode(task.getWbsCode())
                .setDepartmentCode(task.getDepartmentCode())
                .setResourceType(task.getResourceType())
                .setActive(task.getActive())
                .setDisplayOrder(task.getDisplayOrder())
                .setOutlineLevel(task.getOutlineLevel())
                .setCustomerMilestone(task.getCustomerMilestone())
                .setAssignedUserId(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null)
                .setAssignedUserName(task.getAssignedUser() != null ? buildUserDisplayName(task.getAssignedUser()) : null)
                .setDependencies(dependencyDtos)
                .setPredecessorLabel(predecessorLabel);
    }

    private TaskDependencyDto toDependencyDto(TaskDependency dep) {
        TaskDependencyDto dto = new TaskDependencyDto();
        dto.setId(dep.getId());
        dto.setPredecessorTaskId(dep.getPredecessorTaskId());
        dto.setSuccessorTaskId(dep.getSuccessorTaskId());
        dto.setDependencyType(dep.getDependencyType());
        dto.setLagDays(dep.getLagDays());
        return dto;
    }

    private String buildUserDisplayName(AppUser user) {
        if (user == null) {
            return null;
        }

        try {
            return user.getFullName();
        } catch (Exception ex) {
            return user.getUsername();
        }
    }

    private void validateDates(UpdateProjectTaskRequest request) {
        if (request.getPlannedStart() != null && request.getPlannedEnd() != null
                && request.getPlannedEnd().isBefore(request.getPlannedStart())) {
            throw new IllegalArgumentException("Planned end date cannot be before planned start date.");
        }

        if (request.getBaselineStart() != null && request.getBaselineEnd() != null
                && request.getBaselineEnd().isBefore(request.getBaselineStart())) {
            throw new IllegalArgumentException("Baseline end date cannot be before baseline start date.");
        }

        if (request.getActualStart() != null && request.getActualEnd() != null
                && request.getActualEnd().isBefore(request.getActualStart())) {
            throw new IllegalArgumentException("Actual end date cannot be before actual start date.");
        }
    }

    private void validatePercent(Integer value, String label) {
        if (value != null && (value < 0 || value > 100)) {
            throw new IllegalArgumentException(label + " must be between 0 and 100.");
        }
    }

    private void rebuildStructure(Long projectId) {
        List<ProjectTask> tasks = projectTaskRepository.findByProjectIdOrderByDisplayOrderAscIdAsc(projectId);

        int[] levelCounter = new int[20];

        for (int i = 0; i < tasks.size(); i++) {
            ProjectTask task = tasks.get(i);

            int level = task.getOutlineLevel() == null ? 1 : task.getOutlineLevel();

            if (level < 1) {
                level = 1;
            }
            if (level > 10) {
                level = 10;
            }

            task.setOutlineLevel(level);
            task.setDisplayOrder(i + 1);

            levelCounter[level - 1]++;

            for (int j = level; j < levelCounter.length; j++) {
                levelCounter[j] = 0;
            }

            StringBuilder wbs = new StringBuilder();

            for (int j = 0; j < level; j++) {
                if (levelCounter[j] == 0) {
                    break;
                }

                if (wbs.length() > 0) {
                    wbs.append(".");
                }

                wbs.append(levelCounter[j]);
            }

            task.setWbsCode(wbs.toString());
        }

        projectTaskRepository.saveAll(tasks);
    }
}