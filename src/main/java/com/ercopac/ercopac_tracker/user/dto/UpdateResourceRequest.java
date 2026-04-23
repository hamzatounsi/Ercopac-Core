package com.ercopac.ercopac_tracker.user.dto;

import java.math.BigDecimal;

public record UpdateResourceRequest(
        String fullName,
        String employeeCode,
        String departmentCode,
        String resourceType,
        String jobTitle,
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