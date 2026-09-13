package com.ercopac.ercopac_tracker.user.dto;

import java.math.BigDecimal;
import java.util.Set;

public record CreateResourceRequest(
        String fullName,
        String email,
        String password,
        String employeeCode,
        String departmentCode,
        String resourceType,
        String jobTitle,
        Set<String> roles,
        String seniority,
        Boolean internalUser,
        Integer hoursPerDay,
        Integer daysPerWeek,
        String workdays,
        BigDecimal defaultRate,
        String rateType,
        String currency,
        String color,
        String notes,
        Boolean active
) {
}
