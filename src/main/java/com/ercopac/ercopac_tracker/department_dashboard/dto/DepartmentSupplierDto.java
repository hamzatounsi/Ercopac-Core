package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.util.List;

/**
 * A supplier shown in a department.
 *
 * @param resourceTypes           every resource type of the supplier
 * @param departmentResourceTypes the ones in the department scope
 * @param editable                the current user may edit or delete it
 */
public record DepartmentSupplierDto(
        Long id,
        String name,
        String shortCode,
        String country,
        String contact,
        String website,
        List<String> departments,
        List<String> resourceTypes,
        List<String> departmentResourceTypes,
        String notes,
        boolean editable
) {
}
