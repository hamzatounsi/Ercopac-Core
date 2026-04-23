package com.ercopac.ercopac_tracker.platform_dashboard.dto;

public record PlatformKpiDto(
        long totalOrganisations,
        long totalUsers,
        long totalProjects,
        long activeProjects,
        long completedProjects,
        long delayedProjects,
        long criticalProjects
) {
}