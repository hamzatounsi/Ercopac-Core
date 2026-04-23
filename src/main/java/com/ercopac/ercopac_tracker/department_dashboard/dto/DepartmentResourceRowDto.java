package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.util.List;

public record DepartmentResourceRowDto(
        DepartmentMemberDto member,
        List<DepartmentTimelineItemDto> items
) {
}