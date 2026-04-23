package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.util.List;

public record DepartmentProjectBlockDto(
        Long projectId,
        String projectCode,
        String projectName,
        String status,
        List<DepartmentActivityRowDto> rows
) {
}