package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.time.LocalDate;

public record DepartmentTimelineColumnDto(
        LocalDate startDate,
        LocalDate endDate,
        String label,
        String subLabel,
        boolean weekend,
        boolean today
) {
}