package com.ercopac.ercopac_tracker.security;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.user.domain.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DepartmentScopeTest {

    static final Long ORG = 10L;
    static final Long MANAGER_ID = 5L;

    @Mock UserRepository userRepository;
    @Mock ResourceTypeRepository resourceTypeRepository;

    private DepartmentScope departmentScope;
    private Organisation organisation;
    private AppUser manager;

    @BeforeEach
    void setUp() {
        departmentScope = new DepartmentScope(new SecurityUtils(), userRepository, resourceTypeRepository);
        organisation = new Organisation();
        organisation.setId(ORG);
        manager = user(MANAGER_ID, "SW", type(1L, "PM"), Role.DEPARTMENT_MANAGER);
        lenient().when(userRepository.findByIdAndOrganisation_Id(MANAGER_ID, ORG)).thenReturn(Optional.of(manager));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usersWithoutTheDepartmentManagerRoleAreNotRestricted() {
        authenticate("PROJECT_MANAGER");

        DepartmentScope.Scope scope = departmentScope.current();

        assertThat(scope.restricted()).isFalse();
        assertThat(scope.allowsSupplier(supplier(type(9L, "ANY")))).isTrue();
        verifyNoInteractions(userRepository, resourceTypeRepository);
    }

    @Test
    void aBroaderRoleLiftsTheDepartmentRestriction() {
        authenticate("DEPARTMENT_MANAGER", "ORG_ADMIN");

        assertThat(departmentScope.current().restricted()).isFalse();
    }

    @Test
    void departmentManagerGetsTheTypesOfTheirDepartmentPlusTheirOwnType() {
        authenticate("DEPARTMENT_MANAGER");
        lenient().when(resourceTypeRepository.findActiveByOrganisationAndDepartmentCode(ORG, "SW"))
                .thenReturn(List.of(type(2L, "PC"), type(3L, "plc ")));

        DepartmentScope.Scope scope = departmentScope.current();

        assertThat(scope.restricted()).isTrue();
        assertThat(scope.departmentCode()).isEqualTo("SW");
        assertThat(scope.sortedResourceTypeCodes()).containsExactly("PC", "PLC", "PM");
        assertThat(scope.allowsResourceType("plc")).isTrue();
        assertThat(scope.allowsResourceType("MEC")).isFalse();
    }

    @Test
    void fallsBackToTheLegacyDepartmentMapWhenNoTypeIsLinkedToTheDepartment() {
        authenticate("DEPARTMENT_MANAGER");
        lenient().when(resourceTypeRepository.findActiveByOrganisationAndDepartmentCode(ORG, "SW")).thenReturn(List.of());

        assertThat(departmentScope.current().sortedResourceTypeCodes()).containsExactly("PC", "PLC", "PM");
    }

    @Test
    void unknownDepartmentWithoutLinkedTypesFallsBackToItsOwnCode() {
        AppUser other = user(6L, "LAB", null, Role.DEPARTMENT_MANAGER);
        lenient().when(resourceTypeRepository.findActiveByOrganisationAndDepartmentCode(ORG, "LAB")).thenReturn(List.of());

        assertThat(departmentScope.forManager(other, ORG).sortedResourceTypeCodes()).containsExactly("LAB");
    }

    @Test
    void suppliersAreInScopeWhenAnyOfTheirTypesIsAllowed() {
        DepartmentScope.Scope scope = new DepartmentScope.Scope(true, "SW", Set.of("PC", "PLC"));

        assertThat(scope.allowsSupplier(supplier(type(2L, "PC"), type(4L, "MEC")))).isTrue();
        assertThat(scope.allowsSupplier(supplier(type(4L, "MEC")))).isFalse();
        assertThat(scope.allowsSupplier(supplier())).isFalse();
        assertThat(scope.visibleResourceTypes(supplier(type(4L, "MEC"), type(2L, "PC")))).containsExactly("PC");
    }

    @Test
    void resourcesAreInScopeByTypeOrByDepartment() {
        DepartmentScope.Scope scope = new DepartmentScope.Scope(true, "SW", Set.of("PC"));

        assertThat(scope.allowsResource(user(20L, "INST", type(2L, "PC"), Role.EMPLOYEE))).isTrue();
        assertThat(scope.allowsResource(user(21L, "sw", type(4L, "MEC"), Role.EMPLOYEE))).isTrue();
        assertThat(scope.allowsResource(user(22L, "INST", type(4L, "MEC"), Role.EMPLOYEE))).isFalse();
    }

    @Test
    void assertSupplierAccessReturns403OutsideTheScope() {
        authenticate("DEPARTMENT_MANAGER");
        lenient().when(resourceTypeRepository.findActiveByOrganisationAndDepartmentCode(ORG, "SW"))
                .thenReturn(List.of(type(2L, "PC")));

        assertThatThrownBy(() -> departmentScope.assertSupplierAccess(supplier(type(4L, "MEC"))))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }

    // ── fixtures shared with SupplierServiceDepartmentScopeTest ─────────────

    static void authenticate(String... roles) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "manager@test.local", null,
                Arrays.stream(roles).map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
        auth.setDetails(Map.of("organisationId", ORG, "userId", MANAGER_ID));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    static ResourceType type(Long id, String code) {
        ResourceType type = new ResourceType(code, code, null);
        ReflectionTestUtils.setField(type, "id", id);
        return type;
    }

    static Supplier supplier(ResourceType... types) {
        Supplier supplier = new Supplier();
        supplier.setName("SUPPLIER");
        supplier.setActive(true);
        supplier.setResourceTypes(new LinkedHashSet<>(Arrays.asList(types)));
        return supplier;
    }

    AppUser user(Long id, String departmentCode, ResourceType resourceType, Role role) {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "id", id);
        user.setOrganisation(organisation);
        user.setDepartmentCode(departmentCode);
        user.setResourceType(resourceType);
        user.setRoles(Set.of(role));
        user.setActive(true);
        return user;
    }
}
