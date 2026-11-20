package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.util.List;

public record LoadRowDto(
        String resourceType,
        String resourceTypeLabel,
        int peakResourcesNeeded, // Le "Peak"
        int totalHours,          // Le "Σ hours"
        List<LoadCellDto> cells
) {}