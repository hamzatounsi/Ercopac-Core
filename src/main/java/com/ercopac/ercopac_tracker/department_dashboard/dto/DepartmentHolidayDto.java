package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.time.LocalDate;

public record DepartmentHolidayDto(
        Long id,
        Long memberId,
        String memberName,
        LocalDate fromDate,
        LocalDate toDate,
        String note
) {
}