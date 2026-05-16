package com.ercopac.ercopac_tracker.user.dto;

import java.math.BigDecimal;

public record SaveResourceTypeRequest(
        String code,
        String label,
        String colour,
        Long departmentId,
        BigDecimal defaultRate,
        Boolean assignable,
        Boolean active
) {}