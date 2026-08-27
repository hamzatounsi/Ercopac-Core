package com.ercopac.ercopac_tracker.employee_workspace.dto;

import java.time.LocalDate;

/** A deliberately small, employee-safe project summary. */
public record EmployeeProjectDto(
        Long id,
        String code,
        String name,
        String projectManagerName,
        LocalDate plannedStart,
        LocalDate plannedEnd,
        String status,
        Integer progress,
        String health,
        long assignedTaskCount,
        long openActionCount
) {}
