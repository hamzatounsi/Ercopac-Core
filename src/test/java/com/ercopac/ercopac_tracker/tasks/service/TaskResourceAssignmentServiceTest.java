package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskResourceAssignment;
import com.ercopac.ercopac_tracker.tasks.dto.TaskResourceAssignmentDto;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskResourceAssignmentRepository;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.user.domain.Supplier;
import com.ercopac.ercopac_tracker.user.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TaskResourceAssignmentServiceTest {

    @Mock TaskResourceAssignmentRepository assignmentRepository;
    @Mock UserRepository userRepository;
    @Mock ProjectRepository projectRepository;
    @Mock ProjectTaskRepository taskRepository;
    @Mock ResourceTypeRepository resourceTypeRepository;
    @Mock SupplierRepository supplierRepository;
    @Mock SecurityUtils securityUtils;

    private TaskResourceAssignmentService service;
    private Organisation organisation;
    private ResourceType res;
    private ResourceType mech;
    private Supplier supplierA;

    @BeforeEach
    void setUp() {
        service = new TaskResourceAssignmentService(assignmentRepository, userRepository, projectRepository,
                taskRepository, resourceTypeRepository, supplierRepository, securityUtils);
        organisation = new Organisation();
        organisation.setId(10L);
        Project project = new Project();
        project.setId(100L);
        project.setOrganisation(organisation);
        ProjectTask task = new ProjectTask();
        ReflectionTestUtils.setField(task, "id", 200L);
        task.setProjectId(100L);
        task.setOrganisationId(10L);
        task.setTaskType("ACTIVITY");
        res = resourceType(1L, "RES");
        mech = resourceType(2L, "MECH");
        supplierA = new Supplier();
        ReflectionTestUtils.setField(supplierA, "id", 300L);
        supplierA.setOrganisation(organisation);
        supplierA.setName("Supplier A");
        supplierA.setActive(true);
        supplierA.setResourceTypes(new LinkedHashSet<>(java.util.List.of(res)));

        when(securityUtils.getCurrentOrganisationId()).thenReturn(10L);
        when(projectRepository.findByIdAndOrganisationId(100L, 10L)).thenReturn(Optional.of(project));
        when(taskRepository.findById(200L)).thenReturn(Optional.of(task));
        lenient().when(assignmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void persistsValidSupplierAndResourceTypeTogether() {
        when(resourceTypeRepository.findByCodeAndOrganisation_Id("RES", 10L)).thenReturn(Optional.of(res));
        when(supplierRepository.findByIdAndOrganisation_Id(300L, 10L)).thenReturn(Optional.of(supplierA));

        TaskResourceAssignmentDto saved = service.createTaskResource(100L, 200L,
                assignment(300L, "RES"));

        assertThat(saved.getSupplierId()).isEqualTo(300L);
        assertThat(saved.getResourceType()).isEqualTo("RES");
    }

    @Test
    void rejectsSupplierNotLinkedToSelectedResourceType() {
        when(resourceTypeRepository.findByCodeAndOrganisation_Id("MECH", 10L)).thenReturn(Optional.of(mech));
        when(supplierRepository.findByIdAndOrganisation_Id(300L, 10L)).thenReturn(Optional.of(supplierA));

        assertThatThrownBy(() -> service.createTaskResource(100L, 200L, assignment(300L, "MECH")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not linked");
    }

    @Test
    void rejectsSupplierOutsideProjectOrganisation() {
        when(resourceTypeRepository.findByCodeAndOrganisation_Id("RES", 10L)).thenReturn(Optional.of(res));
        when(supplierRepository.findByIdAndOrganisation_Id(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTaskResource(100L, 200L, assignment(999L, "RES")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("project organisation");
    }

    private ResourceType resourceType(Long id, String code) {
        ResourceType resourceType = new ResourceType(code, code, organisation);
        ReflectionTestUtils.setField(resourceType, "id", id);
        return resourceType;
    }

    private TaskResourceAssignmentDto assignment(Long supplierId, String resourceType) {
        return new TaskResourceAssignmentDto()
                .setSupplierId(supplierId)
                .setResourceType(resourceType)
                .setQuantity(1)
                .setUnitsPercent(100);
    }
}
