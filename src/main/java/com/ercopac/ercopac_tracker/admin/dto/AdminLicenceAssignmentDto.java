package com.ercopac.ercopac_tracker.admin.dto;

public record AdminLicenceAssignmentDto(
        Long userId,
        String fullName,
        String email,
        String departmentCode,
        String resourceType,
        String licenceType
) {
}