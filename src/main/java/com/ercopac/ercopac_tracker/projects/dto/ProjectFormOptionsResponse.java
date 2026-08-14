package com.ercopac.ercopac_tracker.projects.dto;

import java.util.List;

public record ProjectFormOptionsResponse(
        List<CategoryOption> categories,
        List<CustomerOption> customers,
        List<UserOption> projectManagers,
        List<UserOption> salesManagers
) {
    public record CategoryOption(Long id, String name) {}
    public record CustomerOption(Long id, String code, String name) {}
    public record UserOption(Long id, String fullName, String departmentCode, String resourceTypeCode) {}
}
