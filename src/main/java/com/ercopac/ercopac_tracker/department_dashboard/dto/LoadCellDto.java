package com.ercopac.ercopac_tracker.department_dashboard.dto;

import java.time.LocalDate;

public record LoadCellDto(
        String label,          // ex: "W47" ou "22 Nov"
        LocalDate startDate,
        LocalDate endDate,
        int resourcesNeeded,   // Le gros chiffre
        int totalHours,        // Le petit chiffre vert
        int capacity,          // ex: 14
        String status          // "OK", "AT_CAPACITY", "OVER_CAPACITY"
) {}