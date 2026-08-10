package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskResourceAssignment;
import com.ercopac.ercopac_tracker.tasks.dto.ResourceUserDto;
import com.ercopac.ercopac_tracker.tasks.dto.TaskResourceAssignmentDto;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskResourceAssignmentRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskResourceAssignmentService {

    private final TaskResourceAssignmentRepository repository;
    private final UserRepository                   userRepository;
    private final ProjectRepository                projectRepository;
    private final ProjectTaskRepository            projectTaskRepository;
    private final ResourceTypeRepository           resourceTypeRepository;
    private final SecurityUtils                    securityUtils;

    public TaskResourceAssignmentService(
            TaskResourceAssignmentRepository repository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            ProjectTaskRepository projectTaskRepository,
            ResourceTypeRepository resourceTypeRepository,
            SecurityUtils securityUtils
    ) {
        this.repository        = repository;
        this.userRepository    = userRepository;
        this.projectRepository = projectRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.securityUtils = securityUtils;
    }

    public List<TaskResourceAssignmentDto> getTaskResources(Long projectId, Long taskId) {
        Project project = getAccessibleProject(projectId);
        validateTaskBelongsToProject(projectId, taskId, project);
        return repository.findByProjectIdAndTaskIdOrderByIdAsc(projectId, taskId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public TaskResourceAssignmentDto createTaskResource(Long projectId, Long taskId,
                                                        TaskResourceAssignmentDto dto) {
        TaskResourceAssignment entity = new TaskResourceAssignment();
        entity.setProjectId(projectId);
        entity.setTaskId(taskId);
        apply(entity, dto, getAccessibleProject(projectId), taskId);
        return toDto(repository.save(entity));
    }

    public TaskResourceAssignmentDto updateTaskResource(Long projectId, Long taskId,
                                                        Long assignmentId,
                                                        TaskResourceAssignmentDto dto) {
        TaskResourceAssignment entity = repository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resource assignment not found: " + assignmentId));

        if (!entity.getProjectId().equals(projectId) || !entity.getTaskId().equals(taskId))
            throw new IllegalArgumentException(
                    "Resource assignment does not belong to the given project/task.");

        Project project = getAccessibleProject(projectId);
        validateTaskBelongsToProject(projectId, taskId, project);
        apply(entity, dto, project, taskId);
        return toDto(repository.save(entity));
    }

    public void deleteTaskResource(Long projectId, Long taskId, Long assignmentId) {
        TaskResourceAssignment entity = repository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resource assignment not found: " + assignmentId));

        if (!entity.getProjectId().equals(projectId) || !entity.getTaskId().equals(taskId))
            throw new IllegalArgumentException(
                    "Resource assignment does not belong to the given project/task.");
        validateTaskBelongsToProject(projectId, taskId, getAccessibleProject(projectId));

        repository.delete(entity);
    }

    public List<ResourceUserDto> getUsersByResourceType(Long projectId, String resourceType) {
        Project project = getAccessibleProject(projectId);
        Long orgId = project.getOrganisation() != null ? project.getOrganisation().getId() : null;

        if (orgId == null) return List.of();

        List<AppUser> users = (resourceType != null && !resourceType.isBlank())
                ? userRepository.findByOrganisationIdAndResourceTypeCodeAndActiveTrue(orgId, resourceType)
                : userRepository.findByOrganisation_IdAndActiveTrueOrderByFullNameAsc(orgId);

        return users.stream()
                .map(u -> new ResourceUserDto(
                        u.getId(),
                        u.getFullName(),
                        u.getResourceType() != null ? u.getResourceType().getCode() : "",
                        u.getDepartmentCode() != null ? u.getDepartmentCode() : "",
                        u.getColor() != null ? u.getColor() : "#3b82f6"))
                .toList();
    }

    private void apply(TaskResourceAssignment entity, TaskResourceAssignmentDto dto, Project project, Long taskId) {
        ProjectTask task = projectTaskRepository.findById(taskId)
                .filter(candidate -> project.getId().equals(candidate.getProjectId()))
                .filter(candidate -> project.getOrganisation().getId().equals(candidate.getOrganisationId()))
                .orElseThrow(() -> new IllegalArgumentException("Task not accessible in project"));
        if (!"ACTIVITY".equalsIgnoreCase(task.getTaskType())) {
            throw new IllegalArgumentException("Resources can only be assigned to ACTIVITY tasks.");
        }
        entity.setResourceType(dto.getResourceType());
        entity.setAssignmentName(dto.getAssignmentName());
        entity.setQuantity(dto.getQuantity() == null ? 1 : dto.getQuantity());
        entity.setUnitsPercent(dto.getUnitsPercent() == null ? 100 : dto.getUnitsPercent());
        entity.setCost(dto.getCost());

        if (dto.getAssignedUserId() != null) {
        userRepository.findByIdAndOrganisation_Id(dto.getAssignedUserId(), project.getOrganisation().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assigned user not found in project organisation: " + dto.getAssignedUserId()));

        entity.setAssignedUserId(dto.getAssignedUserId());
        } else {
        entity.setAssignedUserId(null);
        }

        if (dto.getResourceType() != null && !dto.getResourceType().isBlank()) {
            resourceTypeRepository.findByCodeAndOrganisation_Id(dto.getResourceType(), project.getOrganisation().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Resource type not found in project organisation"));
        }
    }

    private Project getAccessibleProject(Long projectId) {
        if (securityUtils.isPlatformUser()) {
            return projectRepository.findById(projectId)
                    .filter(project -> project.getOrganisation() != null && project.getOrganisation().getId() != null)
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        }

        Long orgId = securityUtils.getCurrentOrganisationId();
        return projectRepository.findByIdAndOrganisationId(projectId, orgId)
                .filter(project -> project.getOrganisation() != null && project.getOrganisation().getId() != null)
                .orElseThrow(() -> new IllegalArgumentException("Project not accessible"));
    }

    private void validateTaskBelongsToProject(Long projectId, Long taskId, Project project) {
        projectTaskRepository.findById(taskId)
                .filter(task -> projectId.equals(task.getProjectId()))
                .filter(task -> project.getOrganisation().getId().equals(task.getOrganisationId()))
                .orElseThrow(() -> new IllegalArgumentException("Task not accessible in project"));
    }

    private TaskResourceAssignmentDto toDto(TaskResourceAssignment entity) {
        return new TaskResourceAssignmentDto()
                .setId(entity.getId())
                .setProjectId(entity.getProjectId())
                .setTaskId(entity.getTaskId())
                .setAssignedUserId(entity.getAssignedUserId())
                .setAssignedUserName(entity.getAssignedUser() != null
                        ? entity.getAssignedUser().getFullName() : null)
                .setResourceType(entity.getResourceType())
                .setAssignmentName(entity.getAssignmentName())
                .setQuantity(entity.getQuantity())
                .setUnitsPercent(entity.getUnitsPercent())
                .setCost(entity.getCost());
    }
}
