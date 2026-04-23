package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.time.LocalDate;

public record DepartmentTimelineItemDto(
        Long projectId,
        String projectCode,
        String projectName,
        Long taskId,
        String taskName,
        String taskType,
        LocalDate startDate,
        LocalDate endDate,
        Integer progressPercent,
        String departmentCode,
        String resourceType,
        boolean doubleBooked,
        boolean holiday,
        String holidayNote,
        Boolean internalResource
) {
}