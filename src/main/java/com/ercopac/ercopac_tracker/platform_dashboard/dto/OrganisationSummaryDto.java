package com.ercopac.ercopac_tracker.platform_dashboard.dto;

public record OrganisationSummaryDto(
        Long organisationId,
        String organisationName,
        long usersCount,
        long projectsCount,
        long activeProjects,
        long delayedProjects,
        long criticalProjects,
        String overallHealth
) {
}