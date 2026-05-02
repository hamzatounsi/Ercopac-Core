package com.ercopac.ercopac_tracker.admin.dto;

public record SaveProjectCategoryRequest(
        String name,
        String code,
        String description,
        String icon,
        String color,
        Boolean active
) {
}