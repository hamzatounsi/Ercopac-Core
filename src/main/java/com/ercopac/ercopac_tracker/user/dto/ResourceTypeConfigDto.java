package com.ercopac.ercopac_tracker.user.dto;

import java.math.BigDecimal;

public record ResourceTypeConfigDto(
        Long id,
        String code,
        String label,
        String colour,
        Long departmentId,
        String departmentCode,
        BigDecimal defaultRate,
        boolean assignable,
        boolean active
) {}