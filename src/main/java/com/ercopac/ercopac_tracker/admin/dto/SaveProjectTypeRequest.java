package com.ercopac.ercopac_tracker.admin.dto;

public record SaveProjectTypeRequest(
        String name,
        String code,
        String description,
        String icon,
        String color,
        Boolean billable,
        Boolean active
) {
}