package com.ercopac.ercopac_tracker.user.dto;

import java.math.BigDecimal;

public record ResourceListItemDto(
        Long id,
        String fullName,
        String employeeCode,
        String departmentCode,
        String resourceType,
        String jobTitle,
        String email,
        String seniority,
        boolean internalUser,
        Integer hoursPerDay,
        Integer daysPerWeek,
        String workdays,
        BigDecimal defaultRate,
        String rateType,
        String currency,
        String color,
        boolean active
) {
}