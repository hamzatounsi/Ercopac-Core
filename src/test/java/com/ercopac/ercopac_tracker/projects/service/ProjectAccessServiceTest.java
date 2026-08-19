package com.ercopac.ercopac_tracker.projects.service;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ProjectAccessServiceTest {

    @Test
    void leadGetsEveryProjectFromTheirOrganisation() {
        ProjectRepository projects = mock(ProjectRepository.class);
        SecurityUtils security = mock(SecurityUtils.class);
        List<Project> organisationProjects = List.of(new Project(), new Project());
        when(security.isPlatformUser()).thenReturn(false);
        when(security.getCurrentOrganisationId()).thenReturn(10L);
        when(security.hasAnyRole("PROJECT_MANAGER_LEAD")).thenReturn(true);
        when(projects.findAllByOrganisationId(10L)).thenReturn(organisationProjects);

        assertSame(organisationProjects, new ProjectAccessService(projects, security).getAccessibleProjects(null));
        verify(projects, never()).findAll();
        verify(projects, never()).findAllByOrganisationIdAndProjectManagerId(anyLong(), anyLong());
    }

    @Test
    void normalProjectManagerRetainsAssignedProjectScope() {
        ProjectRepository projects = mock(ProjectRepository.class);
        SecurityUtils security = mock(SecurityUtils.class);
        List<Project> assignedProjects = List.of(new Project());
        when(security.isPlatformUser()).thenReturn(false);
        when(security.getCurrentOrganisationId()).thenReturn(10L);
        when(security.getCurrentUserId()).thenReturn(55L);
        when(security.hasAnyRole("PROJECT_MANAGER_LEAD")).thenReturn(false);
        when(security.hasAnyRole("PROJECT_MANAGER")).thenReturn(true);
        when(projects.findAllByOrganisationIdAndProjectManagerId(10L, 55L)).thenReturn(assignedProjects);

        assertSame(assignedProjects, new ProjectAccessService(projects, security).getAccessibleProjects(null));
        verify(projects, never()).findAllByOrganisationId(10L);
    }
}
