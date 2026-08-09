package com.ercopac.ercopac_tracker.platform_dashboard.dto;

public record PlatformLicenceUsageDto(String role, String label, int allocated, long used, long available) { }
