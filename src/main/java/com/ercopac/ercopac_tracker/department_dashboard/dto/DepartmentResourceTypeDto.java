package com.ercopac.ercopac_tracker.department_dashboard.dto;

/** A resource type in a department's scope. */
public record DepartmentResourceTypeDto(
        String code,
        String label,
        String colour
) {
}
