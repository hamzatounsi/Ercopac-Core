package com.ercopac.ercopac_tracker.department_dashboard.request;

public record DepartmentOverviewQuery(
        Long managerId,
        String timelineView,
        int offset,
        int span
) {
}