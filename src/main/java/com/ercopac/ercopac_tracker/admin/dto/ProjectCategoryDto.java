package com.ercopac.ercopac_tracker.admin.dto;

public record ProjectCategoryDto(
        Long id,
        String name,
        String code,
        String description,
        String icon,
        String color,
        boolean active,
        long projectsUsing
) {
}