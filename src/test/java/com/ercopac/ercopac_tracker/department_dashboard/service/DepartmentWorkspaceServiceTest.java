package com.ercopac.ercopac_tracker.department_dashboard.service;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentContextDto;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentResourceTypeDto;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentSupplierDto;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.security.PermissionChecker;
import com.ercopac.ercopac_tracker.security.DepartmentScope;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.user.domain.Supplier;
import com.ercopac.ercopac_tracker.user.repository.SupplierRepository;
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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentWorkspaceServiceTest {

    private static final Long ORG = 10L;
    private static final Long MANAGER_ID = 5L;

    @Mock DepartmentDashboardService departmentDashboardService;
    @Mock DepartmentRepository departmentRepository;
    @Mock ResourceTypeRepository resourceTypeRepository;
    @Mock SupplierRepository supplierRepository;
    @Mock PermissionChecker permissionChecker;
    @Mock UserRepository userRepository;

    private DepartmentWorkspaceService service;
    private Organisation organisation;
    private AppUser manager;
    private ResourceType pc;
    private ResourceType mec;

    @BeforeEach
    void setUp() {
        SecurityUtils securityUtils = new SecurityUtils();
        DepartmentScope departmentScope = new DepartmentScope(securityUtils, userRepository, resourceTypeRepository);
        service = new DepartmentWorkspaceService(departmentDashboardService, departmentScope, securityUtils,
                departmentRepository, resourceTypeRepository, supplierRepository, permissionChecker);

        organisation = new Organisation();
        organisation.setId(ORG);
        pc = type(1L, "PC");
        mec = type(2L, "MEC");
        manager = new AppUser();
        ReflectionTestUtils.setField(manager, "id", MANAGER_ID);
        manager.setFullName("Sara Chief");
        manager.setOrganisation(organisation);
        manager.setDepartmentCode("SW");
        manager.setRoles(Set.of(Role.DEPARTMENT_MANAGER));

        lenient().when(departmentDashboardService.resolveManager(any())).thenReturn(manager);
        lenient().when(userRepository.findByIdAndOrganisation_Id(MANAGER_ID, ORG)).thenReturn(Optional.of(manager));
        lenient().when(resourceTypeRepository.findActiveByOrganisationAndDepartmentCode(ORG, "SW")).thenReturn(List.of(pc));
        lenient().when(supplierRepository.findByOrganisation_IdAndActiveTrueOrderByNameAsc(ORG))
                .thenReturn(List.of(supplier(100L, "SW SUPPLIER", pc), supplier(101L, "INST SUPPLIER", mec),
                        supplier(102L, "SHARED SUPPLIER", pc, mec)));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void departmentSuppliersAreLimitedToTheDepartmentResourceTypes() {
        authenticate("DEPARTMENT_MANAGER");
        when(permissionChecker.canWrite(any(), eq(PermissionModule.SUPPLIERS))).thenReturn(true);

        List<DepartmentSupplierDto> suppliers = service.getSuppliers(null);

        assertThat(suppliers).extracting(DepartmentSupplierDto::name).containsExactly("SW SUPPLIER", "SHARED SUPPLIER");
        DepartmentSupplierDto shared = suppliers.get(1);
        assertThat(shared.resourceTypes()).containsExactly("MEC", "PC");
        assertThat(shared.departmentResourceTypes()).containsExactly("PC");
        assertThat(suppliers).allMatch(DepartmentSupplierDto::editable);
    }

    @Test
    void suppliersAreReadOnlyWithoutSupplierWritePermission() {
        authenticate("DEPARTMENT_MANAGER");
        when(permissionChecker.canWrite(any(), eq(PermissionModule.SUPPLIERS))).thenReturn(false);

        assertThat(service.getSuppliers(null)).noneMatch(DepartmentSupplierDto::editable);
    }

    @Test
    void contextDescribesTheDepartmentAndItsScope() {
        authenticate("PROJECT_MANAGER");
        Department department = new Department();
        department.setCode("SW");
        department.setLabel("Software");
        when(departmentRepository.findByCodeAndOrganisation_Id("SW", ORG)).thenReturn(Optional.of(department));
        when(resourceTypeRepository.findByOrganisation_IdAndActiveTrue(ORG)).thenReturn(List.of(mec, pc));
        lenient().when(permissionChecker.canWrite(any(), any())).thenReturn(false);
        lenient().when(permissionChecker.canWrite(any(), eq(PermissionModule.DEPARTMENT_DASHBOARD))).thenReturn(true);

        DepartmentContextDto context = service.getContext(MANAGER_ID);

        assertThat(context.departmentLabel()).isEqualTo("Software");
        assertThat(context.managerName()).isEqualTo("SARA CHIEF");
        assertThat(context.resourceTypes()).extracting(DepartmentResourceTypeDto::code).containsExactly("PC");
        assertThat(context.restricted()).isFalse();
        assertThat(context.canManageHolidays()).isTrue();
        assertThat(context.canManageSuppliers()).isFalse();
    }

    private static void authenticate(String... roles) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user@test.local", null,
                Arrays.stream(roles).map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
        auth.setDetails(Map.of("organisationId", ORG, "userId", MANAGER_ID));
        SecurityContextHolder.getContext().setAuthentication(auth);
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
}
