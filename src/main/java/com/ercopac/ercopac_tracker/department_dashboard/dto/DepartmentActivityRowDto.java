package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.time.LocalDate;

public record DepartmentActivityRowDto(
        Long taskId,
        String wbs,
        String name,
        String type,
        String departmentCode,
        LocalDate baselineStartDate,
        LocalDate baselineEndDate,
        LocalDate actualStartDate,
        LocalDate actualEndDate,
        Integer durationDays,
        Integer progressPercent,
        boolean summary
) {
}