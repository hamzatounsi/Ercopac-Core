package com.ercopac.ercopac_tracker.admin.dto;

/** A tenant-scoped active user that can be reassigned through licence allocation. */
public record AdminLicenceCandidateDto(
        Long userId,
        String fullName,
        String email,
        String departmentCode,
        String resourceType,
        String role
) {
}
