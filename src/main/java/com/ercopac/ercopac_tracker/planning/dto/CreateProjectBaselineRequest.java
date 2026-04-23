package com.ercopac.ercopac_tracker.planning.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateProjectBaselineRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String snapshotJson;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }

    public void setSnapshotJson(String snapshotJson) {
        this.snapshotJson = snapshotJson;
    }
}