package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.time.LocalDate;

public record DepartmentWeeklyStatDto(
        LocalDate weekStart,
        LocalDate weekEnd,
        String resourceType,
        Integer resourceCount,
        Integer plannedHours
) {
}