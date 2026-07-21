package com.ercopac.ercopac_tracker.org_admin.service;

import com.ercopac.ercopac_tracker.auth.passwordreset.PasswordResetRequestRepository;
import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.org_admin.dto.OrgAdminDtos;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.platform_permissions.repository.RolePermissionRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OrganisationAdminServiceTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private OrganisationRepository organisationRepository;
    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private ProjectTaskRepository taskRepository;
    @Mock private PasswordResetRequestRepository passwordResetRepository;
    @Mock private RolePermissionRepository permissionRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private OrganisationAdminService service;
    private Organisation organisation;

    @BeforeEach
    void setUp() {
        service = new OrganisationAdminService(
                securityUtils,
                organisationRepository,
                userRepository,
                departmentRepository,
                taskRepository,
                passwordResetRepository,
                permissionRepository,
                passwordEncoder
        );

        organisation = new Organisation();
        organisation.setId(10L);
        organisation.setName("Tenant One");
        organisation.setCode("TENANT1");
        organisation.setUserLimit(10);
        organisation.setOrgAdminLicenceLimit(2);
        organisation.setGeneralManagerLicenceLimit(2);
        organisation.setDepartmentManagerLicenceLimit(5);
        organisation.setEmployeeLicenceLimit(10);

        when(securityUtils.getCurrentOrganisationId()).thenReturn(10L);
        lenient().when(organisationRepository.findById(10L)).thenReturn(Optional.of(organisation));
    }

    @Test
    void rejectsPlatformOwnerAssignment() {
        OrgAdminDtos.CreateUserRequest request = new OrgAdminDtos.CreateUserRequest(
                "Elevated User",
                "elevated@example.com",
                "temporary-password",
                "PLATFORM_OWNER",
                null,
                null,
                null,
                true
        );

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be assigned");
    }

    @Test
    void crossOrganisationUserCannotBeUpdated() {
        when(userRepository.findByIdAndOrganisation_Id(999L, 10L)).thenReturn(Optional.empty());

        OrgAdminDtos.UpdateUserRequest request = new OrgAdminDtos.UpdateUserRequest(
                "Other Tenant User",
                "other@example.com",
                "EMPLOYEE",
                null,
                null,
                null,
                true
        );

        assertThatThrownBy(() -> service.updateUser(999L, request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void finalActiveOrganisationAdminCannotBeDeactivated() {
        AppUser target = user(21L, Role.ORG_ADMIN, true);
        when(userRepository.findByIdAndOrganisation_Id(21L, 10L)).thenReturn(Optional.of(target));
        when(securityUtils.getCurrentUserId()).thenReturn(22L);
        when(userRepository.countByOrganisation_IdAndRoleAndActiveTrue(10L, Role.ORG_ADMIN))
                .thenReturn(1);

        assertThatThrownBy(() -> service.updateUserStatus(21L, false))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                    assertThat(exception.getStatusCode().value()).isEqualTo(409);
                    assertThat(exception.getReason()).contains("At least one active Organisation Admin");
                });
    }

    @Test
    void departmentFromAnotherOrganisationIsNotAcceptedForUserAssignment() {
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(userRepository.countByOrganisation_IdAndActiveTrue(10L)).thenReturn(1L);
        when(userRepository.countByOrganisation_IdAndRoleAndActiveTrue(10L, Role.EMPLOYEE))
                .thenReturn(0);
        when(departmentRepository.findByIdAndOrganisation_Id(88L, 10L)).thenReturn(Optional.empty());

        OrgAdminDtos.CreateUserRequest request = new OrgAdminDtos.CreateUserRequest(
                "New User",
                "new@example.com",
                "temporary-password",
                "EMPLOYEE",
                88L,
                null,
                null,
                true
        );

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void referencedDepartmentCannotBeDeleted() {
        Department department = new Department("OPS", "Operations", organisation);
        ReflectionTestUtils.setField(department, "id", 55L);
        when(departmentRepository.findByIdAndOrganisation_Id(55L, 10L))
                .thenReturn(Optional.of(department));
        when(userRepository.countOrganisationUsersInDepartment(10L, 55L, "OPS"))
                .thenReturn(2L);

        assertThatThrownBy(() -> service.deleteDepartment(55L))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value()).isEqualTo(409));
    }

    private AppUser user(Long id, Role role, boolean active) {
        AppUser user = new AppUser("Admin", "admin@example.com", "hash", role);
        ReflectionTestUtils.setField(user, "id", id);
        user.setOrganisation(organisation);
        user.setActive(active);
        return user;
    }
}
