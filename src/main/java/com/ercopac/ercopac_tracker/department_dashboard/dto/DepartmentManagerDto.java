package com.ercopac.ercopac_tracker.department_dashboard.dto;

public record DepartmentManagerDto(
        Long id,
        String fullName,
        String email,
        String departmentCode,
        String resourceType,
        String role
) {
}