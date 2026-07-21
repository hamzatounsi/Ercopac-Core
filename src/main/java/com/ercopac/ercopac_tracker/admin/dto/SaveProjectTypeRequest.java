package com.ercopac.ercopac_tracker.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveProjectTypeRequest(
        @NotBlank(message = "Project type name is required")
        @Size(max = 100, message = "Project type name must not exceed 100 characters") String name,
        @NotBlank(message = "Project type code is required")
        @Pattern(regexp = "[A-Za-z0-9_-]{2,30}", message = "Project type code must contain 2 to 30 letters, numbers, hyphens or underscores") String code,
        @Size(max = 500, message = "Description must not exceed 500 characters") String description,
        @Size(max = 80, message = "Icon must not exceed 80 characters") String icon,
        @Pattern(regexp = "#[0-9A-Fa-f]{6}", message = "Colour must be a six-digit hex value") String color,
        Boolean billable,
        Boolean active
) {
}
