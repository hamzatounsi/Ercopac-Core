package com.ercopac.ercopac_tracker.department_dashboard.request;

import java.time.LocalDate;

public record CreateDepartmentHolidayRequest(
        Long memberId,
        LocalDate fromDate,
        LocalDate toDate,
        String note
) {
}