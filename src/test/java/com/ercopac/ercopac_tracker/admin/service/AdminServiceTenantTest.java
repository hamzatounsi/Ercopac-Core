package com.ercopac.ercopac_tracker.admin.service;

import com.ercopac.ercopac_tracker.admin.dto.SaveProjectCategoryRequest;
import com.ercopac.ercopac_tracker.admin.repository.AdminLicenceAssignmentRepository;
import com.ercopac.ercopac_tracker.admin.repository.CustomerRepository;
import com.ercopac.ercopac_tracker.admin.repository.ProjectCategoryRepository;
import com.ercopac.ercopac_tracker.admin.repository.ProjectTypeRepository;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTenantTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private OrganisationRepository organisationRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private AdminLicenceAssignmentRepository licenceRepository;
    @Mock private ProjectCategoryRepository categoryRepository;
    @Mock private ProjectTypeRepository typeRepository;
    @Mock private CustomerRepository customerRepository;

    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService(
                securityUtils,
                organisationRepository,
                userRepository,
                projectRepository,
                licenceRepository,
                categoryRepository,
                typeRepository,
                customerRepository
        );
        when(securityUtils.getCurrentOrganisationId()).thenReturn(10L);
    }

    @Test
    void categoryFromAnotherOrganisationCannotBeUpdated() {
        when(categoryRepository.findByIdAndOrganisation_Id(55L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCategory(55L, categoryRequest("OPS")))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void duplicateCategoryCodeIsReportedAsConflict() {
        when(categoryRepository.existsByOrganisation_IdAndCodeIgnoreCase(10L, "OPS"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.createCategory(categoryRequest("OPS")))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value()).isEqualTo(409));
    }

    private SaveProjectCategoryRequest categoryRequest(String code) {
        return new SaveProjectCategoryRequest(
                "Operations",
                code,
                "Operational programmes",
                "category",
                "#1565c0",
                true
        );
    }
}
