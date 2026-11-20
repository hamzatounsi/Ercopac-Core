package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.util.List;

public record DepartmentLoadResponseDto(
        String departmentCode,
        String timelineView,
        List<DepartmentTimelineColumnDto> timelineColumns,
        List<LoadRowDto> rows,
        int totalPeakResourcesNeeded,
        int totalHours
) {}