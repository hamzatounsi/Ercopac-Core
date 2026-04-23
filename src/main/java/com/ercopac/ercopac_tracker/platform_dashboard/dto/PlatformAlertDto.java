package com.ercopac.ercopac_tracker.platform_dashboard.dto;

public record PlatformAlertDto(
        String type,
        String severity,
        String message
) {
}