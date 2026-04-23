package com.ercopac.ercopac_tracker.platform_dashboard.dto;

import java.time.LocalDate;

public record PlatformProjectRowDto(
        Long projectId,
        String projectCode,
        String projectName,
        String organisationName,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        Integer progressPercent,
        String status,
        String health
) {
}