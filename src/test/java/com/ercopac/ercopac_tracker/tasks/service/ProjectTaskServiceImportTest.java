package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.milestone.repository.MilestoneTypeRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.planning.service.ProjectWorkingDayService;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.projects.service.ProjectAccessService;
import com.ercopac.ercopac_tracker.projects.service.ProjectProgressService;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.dto.UpdateProjectTaskRequest;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskResourceAssignmentRepository;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectTaskServiceImportTest {

    @Mock ProjectTaskRepository projectTaskRepository;
    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;
    @Mock TaskDependencyRepository taskDependencyRepository;
    @Mock TaskResourceAssignmentRepository taskResourceAssignmentRepository;
    @Mock TaskSchedulingService taskSchedulingService;
    @Mock ProjectTaskHistoryService historyService;
    @Mock TaskConsoleService taskConsoleService;
    @Mock ResourceTypeRepository resourceTypeRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock MilestoneTypeRepository milestoneTypeRepository;
    @Mock ProjectAccessService projectAccessService;
    @Mock SecurityUtils securityUtils;
    @Mock ProjectProgressService projectProgressService;
    @Mock ProjectWorkingDayService workingDayService;

    private ProjectTaskService service;

    @BeforeEach
    void setUp() {
        service = new ProjectTaskService(projectTaskRepository, projectRepository, userRepository,
                taskDependencyRepository, taskResourceAssignmentRepository, taskSchedulingService,
                historyService, taskConsoleService, resourceTypeRepository, departmentRepository,
                milestoneTypeRepository, projectAccessService, securityUtils);
        ReflectionTestUtils.setField(service, "projectProgressService", projectProgressService);
        ReflectionTestUtils.setField(service, "workingDayService", workingDayService);
        Organisation organisation = new Organisation();
        organisation.setId(10L);
        Project project = new Project();
        project.setId(100L);
        project.setOrganisation(organisation);
        when(projectAccessService.getAccessibleProject(100L)).thenReturn(project);
    }

    @Test
    void importRecreatesDependenciesFromPredecessorWbs() {
        AtomicLong ids = new AtomicLong(500);
        when(projectTaskRepository.save(any())).thenAnswer(invocation -> {
            ProjectTask task = invocation.getArgument(0);
            if (task.getId() == null) ReflectionTestUtils.setField(task, "id", ids.incrementAndGet());
            return task;
        });

        service.importSchedule(100L, List.of(
                task("1", "SUMMARY", null),
                task("1.1", "ACTIVITY", null),
                task("1.2", "ACTIVITY", "1.1")));

        ArgumentCaptor<TaskDependency> saved = ArgumentCaptor.forClass(TaskDependency.class);
        verify(taskDependencyRepository).save(saved.capture());
        assertThat(saved.getValue().getProjectId()).isEqualTo(100L);
        assertThat(saved.getValue().getPredecessorTaskId()).isEqualTo(502L);
        assertThat(saved.getValue().getSuccessorTaskId()).isEqualTo(503L);
        assertThat(saved.getValue().getDependencyType()).isEqualTo("FS");
        verify(taskSchedulingService).rescheduleFromTask(100L, 502L);
    }

    @Test
    void invalidPredecessorRejectsFileWithRowNumberBeforeTouchingSchedule() {
        assertThatThrownBy(() -> service.importSchedule(100L, List.of(
                task("1", "ACTIVITY", "2"),
                task("2", "ACTIVITY", "1"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Import row 1: predecessors form a cycle");

        assertThatThrownBy(() -> service.importSchedule(100L, List.of(
                task("1", "ACTIVITY", null),
                task("2", "ACTIVITY", "9"))))
                .hasMessage("Import row 2: predecessor WBS '9' is not in the file");

        verify(projectTaskRepository, never()).findByProjectIdOrderByDisplayOrderAscIdAsc(any());
        verify(projectTaskRepository, never()).save(any());
        verify(taskDependencyRepository, never()).save(any());
    }

    private UpdateProjectTaskRequest task(String wbs, String type, String predecessors) {
        UpdateProjectTaskRequest req = new UpdateProjectTaskRequest();
        req.setName("Task " + wbs);
        req.setWbsCode(wbs);
        req.setTaskType(type);
        req.setDurationDays(1);
        req.setPredecessors(predecessors);
        return req;
    }
}
