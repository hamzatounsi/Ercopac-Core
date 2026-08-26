package com.ercopac.ercopac_tracker.employee_workspace.service;

import com.ercopac.ercopac_tracker.employee_workspace.dto.EmployeeProjectDto;
import com.ercopac.ercopac_tracker.projectum.actions.repository.ActionItemRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskResourceAssignmentRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmployeeProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository taskRepository;
    private final ActionItemRepository actionRepository;
    private final TaskResourceAssignmentRepository taskResourceAssignmentRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public EmployeeProjectService(ProjectRepository projectRepository, ProjectTaskRepository taskRepository,
                                  ActionItemRepository actionRepository, TaskResourceAssignmentRepository taskResourceAssignmentRepository,
                                  UserRepository userRepository,
                                  SecurityUtils securityUtils) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.actionRepository = actionRepository;
        this.taskResourceAssignmentRepository = taskResourceAssignmentRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    public List<EmployeeProjectDto> getMyProjects() {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        Long userId = securityUtils.getCurrentUserId();
        String fullName = userRepository.findByIdAndOrganisation_Id(userId, organisationId)
                .map(user -> user.getFullName()).orElse("");

        LinkedHashSet<Long> projectIds = new LinkedHashSet<>();
        projectIds.addAll(taskRepository.findDistinctProjectIdsByAssignedUserIdAndOrganisationId(userId, organisationId));
        projectIds.addAll(taskResourceAssignmentRepository.findDistinctProjectIdsForAssignedUser(userId, organisationId));
        projectIds.addAll(actionRepository.findDistinctProjectIdsForUser(userId, fullName, organisationId));
        if (projectIds.isEmpty()) return List.of();

        return projectRepository.findAllByIdInAndOrganisationIdOrderByNameAsc(List.copyOf(projectIds), organisationId)
                .stream()
                .map(project -> toDto(project, userId, fullName, organisationId))
                .toList();
    }

    private EmployeeProjectDto toDto(Project project, Long userId, String fullName, Long organisationId) {
        return new EmployeeProjectDto(
                project.getId(), project.getCode(), project.getName(), project.getProjectManagerName(),
                project.getPlannedStart(), project.getPlannedEnd(), project.getProjectPhase(), project.getProgress(),
                project.getRiskLevel(),
                taskRepository.countAssignedToUserInProject(project.getId(), userId, organisationId),
                actionRepository.countOpenForUserAndProject(project.getId(), userId, fullName, organisationId)
        );
    }
}
