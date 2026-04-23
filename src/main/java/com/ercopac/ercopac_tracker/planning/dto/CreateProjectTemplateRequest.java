package com.ercopac.ercopac_tracker.planning.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateProjectTemplateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String scope; // "all" or "selected"

    private String description;

    @NotBlank
    private String snapshotJson;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }

    public void setSnapshotJson(String snapshotJson) {
        this.snapshotJson = snapshotJson;
    }
}