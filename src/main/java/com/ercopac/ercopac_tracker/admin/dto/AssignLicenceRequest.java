package com.ercopac.ercopac_tracker.admin.dto;

public record AssignLicenceRequest(
        Long userId,
        String licenceType
) {
}