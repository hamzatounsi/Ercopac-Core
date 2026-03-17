package com.ercopac.ercopac_tracker.gm_dashboard.dto;

import com.ercopac.ercopac_tracker.kpi.domain.HealthStatus;

import java.time.LocalDate;

public record ProjectDashboardRowDto(
        Long id,
        String code,
        String name,
        LocalDate plannedStart,
        LocalDate plannedEnd,
        HealthStatus timeHealth
) {}