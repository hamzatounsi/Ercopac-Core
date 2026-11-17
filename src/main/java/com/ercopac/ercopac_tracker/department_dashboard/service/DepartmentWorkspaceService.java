package com.ercopac.ercopac_tracker.department_dashboard.service;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentContextDto;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentResourceTypeDto;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentSupplierDto;
import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.security.PermissionChecker;
import com.ercopac.ercopac_tracker.security.DepartmentScope;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.domain.Supplier;
import com.ercopac.ercopac_tracker.user.repository.SupplierRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Header data, permissions and suppliers of the department shown on the My Department page.
 * The department is resolved like the overview ({@link DepartmentDashboardService#resolveManager}),
 * and suppliers are limited by {@link DepartmentScope}.
 */
@Service
@Transactional(readOnly = true)
public class DepartmentWorkspaceService {

    private final DepartmentDashboardService departmentDashboardService;
    private final DepartmentScope departmentScope;
    private final SecurityUtils securityUtils;
    private final DepartmentRepository departmentRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final SupplierRepository supplierRepository;
    private final PermissionChecker permissionChecker;

    public DepartmentWorkspaceService(DepartmentDashboardService departmentDashboardService,
                                      DepartmentScope departmentScope,
                                      SecurityUtils securityUtils,
                                      DepartmentRepository departmentRepository,
                                      ResourceTypeRepository resourceTypeRepository,
                                      SupplierRepository supplierRepository,
                                      PermissionChecker permissionChecker) {
        this.departmentDashboardService = departmentDashboardService;
        this.departmentScope = departmentScope;
        this.securityUtils = securityUtils;
        this.departmentRepository = departmentRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.supplierRepository = supplierRepository;
        this.permissionChecker = permissionChecker;
    }

    public DepartmentContextDto getContext(Long managerId) {
        AppUser manager = departmentDashboardService.resolveManager(managerId);
        Long organisationId = securityUtils.getCurrentOrganisationId();
        DepartmentScope.Scope departmentScopeOfManager = departmentScope.forManager(manager, organisationId);
        String departmentCode = manager.getDepartmentCode();

        String departmentLabel = departmentRepository.findByCodeAndOrganisation_Id(departmentCode, organisationId)
                .map(Department::getLabel)
                .filter(label -> label != null && !label.isBlank())
                .orElse(departmentCode);

        List<DepartmentResourceTypeDto> resourceTypes = resourceTypeRepository.findByOrganisation_IdAndActiveTrue(organisationId)
                .stream()
                .filter(type -> departmentScopeOfManager.allowsResourceType(type.getCode()))
                .sorted(Comparator.comparing(ResourceType::getCode))
                .map(type -> new DepartmentResourceTypeDto(type.getCode(), type.getLabel(), type.getColour()))
                .toList();

        return new DepartmentContextDto(
                manager.getId(),
                manager.getFullName(),
                manager.getEmail(),
                manager.getResourceType() == null ? null : manager.getResourceType().getCode(),
                departmentCode,
                departmentLabel,
                resourceTypes,
                departmentScope.current().restricted(),
                canWrite(PermissionModule.SUPPLIERS),
                canWrite(PermissionModule.DEPARTMENT_DASHBOARD),
                canWrite(PermissionModule.RESOURCES)
        );
    }

    /** Active suppliers linked to at least one resource type of the department. */
    public List<DepartmentSupplierDto> getSuppliers(Long managerId) {
        AppUser manager = departmentDashboardService.resolveManager(managerId);
        Long organisationId = securityUtils.getCurrentOrganisationId();
        DepartmentScope.Scope departmentScopeOfManager = departmentScope.forManager(manager, organisationId);
        DepartmentScope.Scope userScope = departmentScope.current();
        boolean canManage = canWrite(PermissionModule.SUPPLIERS);

        return supplierRepository.findByOrganisation_IdAndActiveTrueOrderByNameAsc(organisationId)
                .stream()
                .filter(departmentScopeOfManager::allowsSupplier)
                .map(supplier -> toDto(supplier, departmentScopeOfManager, canManage && userScope.allowsSupplier(supplier)))
                .toList();
    }

    private DepartmentSupplierDto toDto(Supplier supplier, DepartmentScope.Scope scope, boolean editable) {
        return new DepartmentSupplierDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getShortCode(),
                supplier.getCountry(),
                supplier.getContact(),
                supplier.getWebsite(),
                supplier.getDepartmentsCsv() == null ? List.of() : Arrays.stream(supplier.getDepartmentsCsv().split(","))
                        .map(String::trim).filter(value -> !value.isEmpty()).toList(),
                supplier.getResourceTypes().stream().map(ResourceType::getCode).sorted().toList(),
                scope.visibleResourceTypes(supplier),
                supplier.getNotes(),
                editable
        );
    }

    private boolean canWrite(PermissionModule module) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && permissionChecker.canWrite(authentication, module);
    }
}
