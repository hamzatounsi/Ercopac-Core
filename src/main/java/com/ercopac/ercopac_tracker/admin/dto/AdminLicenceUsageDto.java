package com.ercopac.ercopac_tracker.admin.dto;

public record AdminLicenceUsageDto(
        String role,
        String label,
        int limit,
        long used,
        long available,
        boolean unlimited
) {
}
