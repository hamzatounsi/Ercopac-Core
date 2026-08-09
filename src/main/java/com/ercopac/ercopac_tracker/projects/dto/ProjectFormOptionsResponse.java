package com.ercopac.ercopac_tracker.projects.dto;

import java.util.List;

public record ProjectFormOptionsResponse(
        List<CategoryOption> categories,
        List<UserOption> projectManagers,
        List<UserOption> salesManagers
) {
    public record CategoryOption(Long id, String name) {}
    public record UserOption(Long id, String fullName, String departmentCode, String resourceTypeCode) {}
}
