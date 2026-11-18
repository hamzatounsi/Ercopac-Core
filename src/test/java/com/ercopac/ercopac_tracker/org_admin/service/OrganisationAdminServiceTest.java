package com.ercopac.ercopac_tracker.org_admin.service;

import com.ercopac.ercopac_tracker.admin.domain.Customer;
import com.ercopac.ercopac_tracker.admin.repository.CustomerRepository;
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
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.user.UserType;
import com.ercopac.ercopac_tracker.user.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.Set;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

@ExtendWith(MockitoExtension.class)
class OrganisationAdminServiceTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private OrganisationRepository organisationRepository;
    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private ResourceTypeRepository resourceTypeRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private ProjectTaskRepository taskRepository;
    @Mock private PasswordResetRequestRepository passwordResetRepository;
    @Mock private RolePermissionRepository permissionRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CustomerRepository customerRepository;

    private OrganisationAdminService service;
    private Organisation organisation;

    @BeforeEach
    void setUp() {
        service = new OrganisationAdminService(
                securityUtils,
                organisationRepository,
                userRepository,
                departmentRepository,
                resourceTypeRepository,
                supplierRepository,
                taskRepository,
                passwordResetRepository,
                permissionRepository,
                passwordEncoder,
                customerRepository
        );

        organisation = new Organisation();
        organisation.setId(10L);
        organisation.setName("Tenant One");
        organisation.setCode("TENANT1");
        organisation.setUserLimit(10);
        organisation.setOrgAdminLicenceLimit(2);
        organisation.setProjectManagerLicenceLimit(2);
        organisation.setDepartmentManagerLicenceLimit(5);
        organisation.setEmployeeLicenceLimit(10);
        organisation.setSalesManagerLicenceLimit(5);
        organisation.setClientLicenceLimit(5);

        when(securityUtils.getCurrentOrganisationId()).thenReturn(10L);
        lenient().when(organisationRepository.findById(10L)).thenReturn(Optional.of(organisation));
    }

    @Test
    void rejectsPlatformOwnerAssignment() {
        OrgAdminDtos.CreateUserRequest request = new OrgAdminDtos.CreateUserRequest(
                "Elevated User",
                "elevated@example.com",
                "temporary-password",
                Set.of("PLATFORM_OWNER"),
                null,
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
                Set.of("EMPLOYEE"),
                null,
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
                Set.of("EMPLOYEE"),
                88L,
                null,
                null,
                null,
                true
        );

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void crmRolesUseTheSharedSalesSeatWithoutRequiringAResourceProfile() {
        when(passwordEncoder.encode("temporary-password")).thenReturn("encoded-password");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        createOrganisationUser("sales-lead@example.com", "SALES_MANAGER_LEAD");
        createOrganisationUser("sales@example.com", "SALES_MANAGER");
        createOrganisationUser("engineer@example.com", "SYSTEM_ENGINEER");

        ArgumentCaptor<AppUser> users = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository, org.mockito.Mockito.times(3)).save(users.capture());

        assertThat(users.getAllValues())
                .extracting(AppUser::getRoles, AppUser::getOrganisation, AppUser::isActive, AppUser::isInternalUser)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(Set.of(Role.SALES_MANAGER_LEAD), organisation, true, false),
                        org.assertj.core.groups.Tuple.tuple(Set.of(Role.SALES_MANAGER), organisation, true, false),
                        org.assertj.core.groups.Tuple.tuple(Set.of(Role.SYSTEM_ENGINEER), organisation, true, false)
                );
    }

    @Test
    void createsOneUserWithMultipleRoles() {
        when(passwordEncoder.encode("temporary-password")).thenReturn("encoded-password");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Department department = new Department();
        ReflectionTestUtils.setField(department, "id", 20L);
        department.setCode("PMO");
        department.setLabel("PMO");
        department.setOrganisation(organisation);
        ResourceType resourceType = resourceType(30L, "PM", organisation);
        when(departmentRepository.findByIdAndOrganisation_Id(20L, 10L)).thenReturn(Optional.of(department));
        when(resourceTypeRepository.findByIdAndOrganisation_Id(30L, 10L)).thenReturn(Optional.of(resourceType));

        OrgAdminDtos.UserSummary summary = service.createUser(new OrgAdminDtos.CreateUserRequest(
                "Project Sales", "project-sales@example.com", "temporary-password",
                Set.of("PROJECT_MANAGER", "SALES_MANAGER"), 20L, 30L, null, null, true));

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRoles()).containsExactlyInAnyOrder(
                Role.PROJECT_MANAGER, Role.SALES_MANAGER);
        assertThat(summary.roles()).containsExactly("PROJECT_MANAGER", "SALES_MANAGER");
    }

    @Test
    void updatesMultipleRolesAndAcceptsLegacySingleRoleRequests() {
        AppUser target = user(21L, Role.CLIENT, true);
        when(userRepository.findByIdAndOrganisation_Id(21L, 10L)).thenReturn(Optional.of(target));
        when(securityUtils.getCurrentUserId()).thenReturn(99L);
        when(userRepository.save(target)).thenReturn(target);

        OrgAdminDtos.UserSummary withSecondRole = service.updateUser(21L, new OrgAdminDtos.UpdateUserRequest(
                "Admin", "admin@example.com", Set.of("CLIENT", "SALES_MANAGER"),
                null, null, null, null, true));
        assertThat(withSecondRole.roles()).containsExactly("CLIENT", "SALES_MANAGER");

        OrgAdminDtos.UserSummary withoutSecondRole = service.updateUser(21L, new OrgAdminDtos.UpdateUserRequest(
                "Admin", "admin@example.com", Set.of("CLIENT"),
                null, null, null, null, true));
        assertThat(withoutSecondRole.roles()).containsExactly("CLIENT");

        // ✅ CORRECTION ICI : Ajout de ", null" à la fin pour correspondre au nouveau constructeur à 9 arguments
        OrgAdminDtos.UserSummary legacyRequest = service.updateUser(21L, new OrgAdminDtos.UpdateUserRequest(
                "Admin", "admin@example.com", null,
                null, null, null, null, true, "CLIENT", null));
        assertThat(legacyRequest.roles()).containsExactly("CLIENT");
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

    @Test
    void supplierCanBeLinkedToMultipleOrganisationResourceTypes() {
        ResourceType res = resourceType(41L, "RES", organisation);
        ResourceType elec = resourceType(42L, "ELEC", organisation);
        when(resourceTypeRepository.findAllById(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(res, elec));
        when(supplierRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    var supplier = invocation.getArgument(0, com.ercopac.ercopac_tracker.user.domain.Supplier.class);
                    ReflectionTestUtils.setField(supplier, "id", 91L);
                    return supplier;
                });

        OrgAdminDtos.SupplierSummary saved = service.createSupplier(supplierRequest(List.of(41L, 42L)));

        assertThat(saved.resourceTypes()).extracting(OrgAdminDtos.SupplierResourceTypeSummary::code)
                .containsExactly("RES", "ELEC");
    }

    @Test
    void supplierRejectsResourceTypeFromAnotherOrganisation() {
        Organisation other = new Organisation();
        other.setId(20L);
        ResourceType foreign = resourceType(99L, "MECH", other);
        when(resourceTypeRepository.findAllById(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(foreign));

        assertThatThrownBy(() -> service.createSupplier(supplierRequest(List.of(99L))))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                    assertThat(exception.getStatusCode().value()).isEqualTo(400);
                    assertThat(exception.getReason()).contains("supplier organisation");
                });
    }

    private OrgAdminDtos.SaveSupplierRequest supplierRequest(List<Long> resourceTypeIds) {
        return new OrgAdminDtos.SaveSupplierRequest(
                "Supplier A", "SUP-001", null, null, null, null, null, resourceTypeIds, true);
    }

    private ResourceType resourceType(Long id, String code, Organisation owner) {
        ResourceType resourceType = new ResourceType(code, code, owner);
        ReflectionTestUtils.setField(resourceType, "id", id);
        return resourceType;
    }

    private AppUser user(Long id, Role role, boolean active) {
        AppUser user = new AppUser("Admin", "admin@example.com", "hash", role);
        ReflectionTestUtils.setField(user, "id", id);
        user.setOrganisation(organisation);
        user.setActive(active);
        return user;
    }

    private void createOrganisationUser(String email, String role) {
        createOrganisationUser(email, role, null, null);
    }

    private void createOrganisationUser(String email, String role, Long departmentId, Long resourceTypeId) {
        service.createUser(new OrgAdminDtos.CreateUserRequest(
                "New " + role,
                email,
                "temporary-password",
                Set.of(role),
                departmentId,
                resourceTypeId,
                null,
                null,
                true
        ));
    }

    // ---------- Customer users ----------

    @Test
    void usersPageListsInternalUsersOnlyWithTheirOrganisationName() {
        AppUser internal = user(21L, Role.EMPLOYEE, true);
        when(userRepository.searchOrganisationUsers(eq(10L), any(), any(), any(), any(), eq(UserType.INTERNAL), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(internal)));

        OrgAdminDtos.PageResponse<OrgAdminDtos.UserSummary> page = service.getUsers(null, null, null, null, 0, 20, "fullName", "asc");

        assertThat(page.content()).singleElement().satisfies(summary -> {
            assertThat(summary.userType()).isEqualTo("INTERNAL");
            assertThat(summary.organisationName()).isEqualTo("TENANT ONE");
            assertThat(summary.customerId()).isNull();
        });
    }

    @Test
    void customerUsersPageListsCustomerUsersOnly() {
        Customer acme = customer(7L, organisation, true);
        AppUser client = customerUser(31L, acme);
        when(userRepository.searchOrganisationUsers(eq(10L), any(), isNull(), isNull(), any(), eq(UserType.CUSTOMER), eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(client)));

        OrgAdminDtos.PageResponse<OrgAdminDtos.UserSummary> page = service.getCustomerUsers(null, 7L, null, 0, 20, "fullName", "asc");

        assertThat(page.content()).singleElement().satisfies(summary -> {
            assertThat(summary.userType()).isEqualTo("CUSTOMER");
            assertThat(summary.customerId()).isEqualTo(7L);
            assertThat(summary.customerName()).isEqualTo("ACME");
            assertThat(summary.roles()).containsExactly("CLIENT");
        });
    }

    @Test
    void createsCustomerUserWithClientRoleOnlyAndNoResourceProfile() {
        Customer acme = customer(7L, organisation, true);
        when(customerRepository.findByIdAndOrganisation_Id(7L, 10L)).thenReturn(Optional.of(acme));
        when(passwordEncoder.encode("temporary-password")).thenReturn("encoded-password");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrgAdminDtos.UserSummary summary = service.createCustomerUser(new OrgAdminDtos.CreateCustomerUserRequest(
                "Jane Buyer", "Jane@Acme.test", "temporary-password", "Buyer", 7L, true));

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        AppUser saved = captor.getValue();
        assertThat(saved.getUserType()).isEqualTo(UserType.CUSTOMER);
        assertThat(saved.getCustomer()).isSameAs(acme);
        assertThat(saved.getRoles()).containsExactly(Role.CLIENT);
        assertThat(saved.getOrganisation()).isSameAs(organisation);
        assertThat(saved.isInternalUser()).isFalse();
        assertThat(saved.getDepartment()).isNull();
        assertThat(saved.getResourceType()).isNull();
        assertThat(saved.getEmployeeCode()).isNull();
        assertThat(saved.getEmail()).isEqualTo("jane@acme.test");
        assertThat(summary.customerName()).isEqualTo("ACME");
    }

    @Test
    void customerUserRequiresACustomer() {
        assertThatThrownBy(() -> service.createCustomerUser(new OrgAdminDtos.CreateCustomerUserRequest(
                "Jane Buyer", "jane@acme.test", "temporary-password", null, null, true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customer is required");
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void customerUserCannotUseACustomerOfAnotherOrganisation() {
        when(customerRepository.findByIdAndOrganisation_Id(70L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCustomerUser(new OrgAdminDtos.CreateCustomerUserRequest(
                "Jane Buyer", "jane@acme.test", "temporary-password", null, 70L, true)))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value()).isEqualTo(404));
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void customerUserCannotBeLinkedToAnInactiveCustomer() {
        when(customerRepository.findByIdAndOrganisation_Id(7L, 10L)).thenReturn(Optional.of(customer(7L, organisation, false)));

        assertThatThrownBy(() -> service.createCustomerUser(new OrgAdminDtos.CreateCustomerUserRequest(
                "Jane Buyer", "jane@acme.test", "temporary-password", null, 7L, true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void internalUsersCannotBeCreatedWithTheClientRole() {
        assertThatThrownBy(() -> createOrganisationUser("client@example.com", "CLIENT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer Users");
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void internalUserCannotGainTheClientRole() {
        AppUser target = user(21L, Role.EMPLOYEE, true);
        when(userRepository.findByIdAndOrganisation_Id(21L, 10L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> service.updateUser(21L, new OrgAdminDtos.UpdateUserRequest(
                "Employee", "employee@example.com", Set.of("EMPLOYEE", "CLIENT"), null, null, null, null, true)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void usersEndpointCannotEditACustomerUser() {
        AppUser client = customerUser(31L, customer(7L, organisation, true));
        when(userRepository.findByIdAndOrganisation_Id(31L, 10L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.updateUser(31L, new OrgAdminDtos.UpdateUserRequest(
                "Jane Buyer", "jane@acme.test", Set.of("EMPLOYEE"), null, null, null, null, true)))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void customerUsersEndpointCannotEditAnInternalUser() {
        when(userRepository.findByIdAndOrganisation_Id(21L, 10L)).thenReturn(Optional.of(user(21L, Role.EMPLOYEE, true)));

        assertThatThrownBy(() -> service.updateCustomerUser(21L, new OrgAdminDtos.UpdateCustomerUserRequest(
                "Employee", "employee@example.com", null, null, 7L, true)))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void customerUserCanMoveToAnotherCustomerOfTheOrganisation() {
        Customer acme = customer(7L, organisation, true);
        Customer stark = customer(8L, organisation, true);
        AppUser client = customerUser(31L, acme);
        when(userRepository.findByIdAndOrganisation_Id(31L, 10L)).thenReturn(Optional.of(client));
        when(customerRepository.findByIdAndOrganisation_Id(8L, 10L)).thenReturn(Optional.of(stark));
        when(userRepository.save(client)).thenReturn(client);

        OrgAdminDtos.UserSummary summary = service.updateCustomerUser(31L, new OrgAdminDtos.UpdateCustomerUserRequest(
                "Jane Buyer", "jane@acme.test", null, "Lead buyer", 8L, true));

        assertThat(client.getCustomer()).isSameAs(stark);
        assertThat(client.getRoles()).containsExactly(Role.CLIENT);
        assertThat(summary.customerId()).isEqualTo(8L);
    }

    private Customer customer(Long id, Organisation owner, boolean active) {
        Customer customer = new Customer();
        ReflectionTestUtils.setField(customer, "id", id);
        customer.setOrganisation(owner);
        customer.setName("Acme");
        customer.setCustomerCode("CUST-" + id);
        customer.setActive(active);
        return customer;
    }

    private AppUser customerUser(Long id, Customer customer) {
        AppUser client = user(id, Role.CLIENT, true);
        client.setUserType(UserType.CUSTOMER);
        client.setCustomer(customer);
        client.setInternalUser(false);
        return client;
    }

}