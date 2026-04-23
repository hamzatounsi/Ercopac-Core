package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.util.List;

public record DepartmentMemberDto(
        Long id,
        String fullName,
        String employeeCode,
        String email,
        String departmentCode,
        String resourceType,
        String role,
        String seniority,
        boolean internal,
        Integer hoursPerDay,
        Integer daysPerWeek,
        List<Integer> workdays,
        String color
) {
}