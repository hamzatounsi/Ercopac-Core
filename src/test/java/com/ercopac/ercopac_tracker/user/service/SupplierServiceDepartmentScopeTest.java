package com.ercopac.ercopac_tracker.user.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.DepartmentScope;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.repository.TaskResourceAssignmentRepository;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.user.domain.Supplier;
import com.ercopac.ercopac_tracker.user.dto.CreateResourceRequest;
import com.ercopac.ercopac_tracker.user.dto.CreateSupplierRequest;
import com.ercopac.ercopac_tracker.user.dto.SupplierDto;
import com.ercopac.ercopac_tracker.user.dto.UpdateSupplierRequest;
import com.ercopac.ercopac_tracker.user.repository.SupplierRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Department Chief supplier restriction: a Department Manager only lists, views, creates, edits and
 * deletes suppliers linked to the resource types of their department (SW → PC, PLC). Resources follow
 * the same rule.
 */
@ExtendWith(MockitoExtension.class)
class SupplierServiceDepartmentScopeTest {

    private static final Long ORG = 10L;
    private static final Long MANAGER_ID = 5L;

    @Mock SupplierRepository supplierRepository;
    @Mock OrganisationRepository organisationRepository;
    @Mock ResourceTypeRepository resourceTypeRepository;
    @Mock UserRepository userRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TaskResourceAssignmentRepository taskResourceAssignmentRepository;

    private SupplierService supplierService;
    private ResourceService resourceService;
    private Organisation organisation;
    private ResourceType pc;
    private ResourceType plc;
    private ResourceType mec;
    private Supplier swSupplier;
    private Supplier instSupplier;
    private Supplier sharedSupplier;

    @BeforeEach
    void setUp() {
        SecurityUtils securityUtils = new SecurityUtils();
        DepartmentScope departmentScope = new DepartmentScope(securityUtils, userRepository, resourceTypeRepository);
        supplierService = new SupplierService(supplierRepository, organisationRepository, securityUtils,
                resourceTypeRepository, departmentScope);
        resourceService = new ResourceService(userRepository, organisationRepository, resourceTypeRepository,
                departmentRepository, passwordEncoder, securityUtils, taskResourceAssignmentRepository, departmentScope);

        organisation = new Organisation();
        organisation.setId(ORG);
        pc = type(1L, "PC");
        plc = type(2L, "PLC");
        mec = type(3L, "MEC");
        swSupplier = supplier(100L, "SW SUPPLIER", pc);
        instSupplier = supplier(101L, "INST SUPPLIER", mec);
        sharedSupplier = supplier(102L, "SHARED SUPPLIER", plc, mec);

        AppUser manager = user(MANAGER_ID, "SW", null, Role.DEPARTMENT_MANAGER);
        lenient().when(userRepository.findByIdAndOrganisation_Id(MANAGER_ID, ORG)).thenReturn(Optional.of(manager));
        lenient().when(resourceTypeRepository.findActiveByOrganisationAndDepartmentCode(ORG, "SW")).thenReturn(List.of(pc, plc));
        for (ResourceType type : List.of(pc, plc, mec)) {
            lenient().when(resourceTypeRepository.findByCodeAndOrganisation_Id(type.getCode(), ORG)).thenReturn(Optional.of(type));
        }
        for (Supplier supplier : List.of(swSupplier, instSupplier, sharedSupplier)) {
            lenient().when(supplierRepository.findByIdAndOrganisation_Id(supplier.getId(), ORG)).thenReturn(Optional.of(supplier));
        }
        lenient().when(supplierRepository.findByOrganisation_IdAndActiveTrueOrderByNameAsc(ORG))
                .thenReturn(List.of(instSupplier, sharedSupplier, swSupplier));
        lenient().when(organisationRepository.findById(ORG)).thenReturn(Optional.of(organisation));
        lenient().when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void departmentManagerListsOnlySuppliersOfTheirResourceTypes() {
        authenticate("DEPARTMENT_MANAGER");

        assertThat(supplierService.getSuppliers()).extracting(SupplierDto::name)
                .containsExactly("SHARED SUPPLIER", "SW SUPPLIER");
    }

    @Test
    void organisationAdminStillListsEverySupplier() {
        authenticate("ORG_ADMIN");

        assertThat(supplierService.getSuppliers()).hasSize(3);
    }

    @Test
    void departmentManagerGetsForbiddenForASupplierOutsideTheirTypes() {
        authenticate("DEPARTMENT_MANAGER");

        assertThat(supplierService.getSupplier(100L).name()).isEqualTo("SW SUPPLIER");
        assertForbidden(() -> supplierService.getSupplier(101L));
    }

    @Test
    void departmentManagerCannotEditOrDeleteASupplierOutsideTheirTypes() {
        authenticate("DEPARTMENT_MANAGER");

        assertForbidden(() -> supplierService.updateSupplier(101L, update("INST SUPPLIER", List.of("MEC"))));
        assertForbidden(() -> supplierService.deleteSupplier(101L));
        verify(supplierRepository, never()).save(any());
        assertThat(instSupplier.isActive()).isTrue();
    }

    @Test
    void departmentManagerCanDeleteASupplierOfTheirTypes() {
        authenticate("DEPARTMENT_MANAGER");

        supplierService.deleteSupplier(100L);

        assertThat(swSupplier.isActive()).isFalse();
    }

    @Test
    void departmentManagerCannotCreateASupplierWithAnotherDepartmentsType() {
        authenticate("DEPARTMENT_MANAGER");

        assertForbidden(() -> supplierService.createSupplier(create("NEW", List.of("PC", "MEC"))));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void departmentManagerMustGiveOneOfTheirTypesWhenCreating() {
        authenticate("DEPARTMENT_MANAGER");

        assertThatThrownBy(() -> supplierService.createSupplier(create("NEW", List.of())))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void departmentManagerCreatesASupplierInTheirDepartment() {
        authenticate("DEPARTMENT_MANAGER");

        SupplierDto created = supplierService.createSupplier(create("new supplier", List.of("pc")));

        assertThat(created.resourceTypes()).containsExactly("PC");
        assertThat(created.departments()).containsExactly("SW");
    }

    @Test
    void editingASharedSupplierKeepsTheTypesTheManagerCannotSee() {
        authenticate("DEPARTMENT_MANAGER");

        SupplierDto updated = supplierService.updateSupplier(102L, update("SHARED SUPPLIER", List.of("PC")));

        assertThat(updated.resourceTypes()).containsExactly("MEC", "PC");
        ArgumentCaptor<Supplier> saved = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(saved.capture());
        assertThat(saved.getValue().getResourceTypes()).extracting(ResourceType::getCode)
                .containsExactlyInAnyOrder("PC", "MEC");
    }

    @Test
    void editingASharedSupplierCannotAddAnotherDepartmentsType() {
        authenticate("DEPARTMENT_MANAGER");
        ResourceType elect = type(4L, "ELECT");
        when(resourceTypeRepository.findByCodeAndOrganisation_Id("ELECT", ORG)).thenReturn(Optional.of(elect));

        assertForbidden(() -> supplierService.updateSupplier(102L, update("SHARED SUPPLIER", List.of("PLC", "MEC", "ELECT"))));
    }

    @Test
    void unknownSupplierIsNotFound() {
        authenticate("DEPARTMENT_MANAGER");
        when(supplierRepository.findByIdAndOrganisation_Id(999L, ORG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.getSupplier(999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void resourcesFollowTheSameRule() {
        authenticate("DEPARTMENT_MANAGER");
        AppUser instEngineer = user(30L, "INST", mec, Role.EMPLOYEE);
        AppUser swEngineer = user(31L, "SW", pc, Role.EMPLOYEE);
        when(userRepository.findByIdAndOrganisation_Id(30L, ORG)).thenReturn(Optional.of(instEngineer));
        when(userRepository.findByIdAndOrganisation_Id(31L, ORG)).thenReturn(Optional.of(swEngineer));

        assertThat(resourceService.getResourceById(31L).fullName()).isEqualTo("USER 31");
        assertForbidden(() -> resourceService.getResourceById(30L));
        assertForbidden(() -> resourceService.updateResourceStatus(30L, false));
        assertThat(instEngineer.isActive()).isTrue();
    }

    @Test
    void departmentManagerCannotCreateAResourceWithAnotherDepartmentsType() {
        authenticate("DEPARTMENT_MANAGER");
        CreateResourceRequest request = new CreateResourceRequest("NEW ENGINEER", "new@test.local", "secret123",
                null, "INST", "MEC", null, Set.of("EMPLOYEE"), null, true, 8, 5, "MON-FRI",
                null, null, null, null, null, true);

        assertForbidden(() -> resourceService.createResource(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resourceTypeOptionsAreLimitedToTheScope() {
        authenticate("DEPARTMENT_MANAGER");
        when(userRepository.findDistinctResourceTypesByOrganisationId(ORG)).thenReturn(List.of("MEC", "PC", "PLC"));

        assertThat(resourceService.getResourceTypes()).containsExactly("PC", "PLC");
    }

    private void assertForbidden(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }

    private static void authenticate(String... roles) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user@test.local", null,
                Arrays.stream(roles).map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
        auth.setDetails(Map.of("organisationId", ORG, "userId", MANAGER_ID));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private static CreateSupplierRequest create(String name, List<String> types) {
        return new CreateSupplierRequest(name, null, null, null, null, null, types, null);
    }

    private static UpdateSupplierRequest update(String name, List<String> types) {
        return new UpdateSupplierRequest(name, null, null, null, null, null, types, null);
    }

    private ResourceType type(Long id, String code) {
        ResourceType type = new ResourceType(code, code, organisation);
        ReflectionTestUtils.setField(type, "id", id);
        return type;
    }

    private Supplier supplier(Long id, String name, ResourceType... types) {
        Supplier supplier = new Supplier();
        ReflectionTestUtils.setField(supplier, "id", id);
        supplier.setOrganisation(organisation);
        supplier.setName(name);
        supplier.setActive(true);
        supplier.setResourceTypes(new LinkedHashSet<>(Arrays.asList(types)));
        return supplier;
    }

    private AppUser user(Long id, String departmentCode, ResourceType resourceType, Role role) {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "id", id);
        user.setFullName("User " + id);
        user.setEmail("user" + id + "@test.local");
        user.setOrganisation(organisation);
        user.setDepartmentCode(departmentCode);
        user.setResourceType(resourceType);
        user.setRoles(Set.of(role));
        user.setActive(true);
        user.setInternalUser(true);
        return user;
    }
}
