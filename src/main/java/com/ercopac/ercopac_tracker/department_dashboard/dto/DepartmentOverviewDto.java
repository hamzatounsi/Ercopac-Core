package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.util.List;

public record DepartmentOverviewDto(
        DepartmentManagerDto selectedManager,
        String selectedDepartmentCode,
        List<DepartmentMemberDto> members,
        List<DepartmentHolidayDto> holidays,
        List<DepartmentTimelineColumnDto> timelineColumns,
        List<DepartmentResourceRowDto> resourceRows,
        List<DepartmentProjectBlockDto> projectBlocks,
        List<DepartmentWeeklyStatDto> weeklyStats
) {
}