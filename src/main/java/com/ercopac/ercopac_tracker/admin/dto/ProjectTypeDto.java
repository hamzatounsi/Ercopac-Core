package com.ercopac.ercopac_tracker.admin.dto;

public record ProjectTypeDto(
        Long id,
        String name,
        String code,
        String description,
        String icon,
        String color,
        boolean billable,
        boolean active,
        long projectsUsing
) {
}