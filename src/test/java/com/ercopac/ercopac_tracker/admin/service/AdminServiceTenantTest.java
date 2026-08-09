package com.ercopac.ercopac_tracker.admin.service;

import com.ercopac.ercopac_tracker.admin.dto.SaveProjectCategoryRequest;
import com.ercopac.ercopac_tracker.admin.repository.AdminLicenceAssignmentRepository;
import com.ercopac.ercopac_tracker.admin.repository.CustomerRepository;
import com.ercopac.ercopac_tracker.admin.repository.ProjectCategoryRepository;
import com.ercopac.ercopac_tracker.admin.repository.ProjectTypeRepository;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.Role;
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

    @Test
    void licenceUsageUsesAllOrganisationRolesExceptPlatformOwner() {
        Organisation organisation = new Organisation();
        organisation.setId(10L);
        organisation.setOrgAdminLicenceLimit(2);
        organisation.setProjectManagerLicenceLimit(3);
        organisation.setDepartmentManagerLicenceLimit(4);
        organisation.setEmployeeLicenceLimit(5);
        organisation.setSalesManagerLicenceLimit(3);
        organisation.setClientLicenceLimit(2);
        when(organisationRepository.findById(10L)).thenReturn(Optional.of(organisation));
        when(userRepository.countByOrganisation_IdAndRoleAndActiveTrue(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(Role.class)
        )).thenReturn(0);
        when(userRepository.countByOrganisation_IdAndRoleAndActiveTrue(10L, Role.SALES_MANAGER)).thenReturn(2);
        when(userRepository.countByOrganisation_IdAndRoleAndActiveTrue(10L, Role.CLIENT)).thenReturn(1);

        var usage = service.getLicenceUsage();

        assertThat(usage).extracting(item -> item.role())
                .containsExactlyInAnyOrder(
                        "ORG_ADMIN", "PROJECT_MANAGER", "DEPARTMENT_MANAGER",
                        "EMPLOYEE", "SALES_MANAGER", "CLIENT"
                )
                .doesNotContain("PLATFORM_OWNER");
        assertThat(usage.stream().filter(item -> item.role().equals("SALES_MANAGER")).findFirst().orElseThrow())
                .extracting(item -> item.limit(), item -> item.used(), item -> item.available(), item -> item.unlimited())
                .containsExactly(3, 2L, 1L, false);
        assertThat(usage.stream().filter(item -> item.role().equals("CLIENT")).findFirst().orElseThrow())
                .extracting(item -> item.limit(), item -> item.used(), item -> item.available(), item -> item.unlimited())
                .containsExactly(2, 1L, 1L, false);
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
