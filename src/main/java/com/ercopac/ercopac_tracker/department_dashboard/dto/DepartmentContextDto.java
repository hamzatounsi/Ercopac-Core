package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.util.List;

/**
 * Header data and permissions for the My Department page.
 *
 * @param resourceTypes the department scope (see DepartmentScope); suppliers created here must use one of them
 * @param restricted    true when the current user is limited to this scope (a Department Manager)
 */
public record DepartmentContextDto(
        Long managerId,
        String managerName,
        String managerEmail,
        String managerResourceType,
        String departmentCode,
        String departmentLabel,
        List<DepartmentResourceTypeDto> resourceTypes,
        boolean restricted,
        boolean canManageSuppliers,
        boolean canManageHolidays,
        boolean canManageResources
) {
}
