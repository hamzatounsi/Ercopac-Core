package com.ercopac.ercopac_tracker.planning.dto;

import java.time.Instant;

public class ProjectBaselineDto {

    private Long id;
    private Long projectId;
    private String name;
    private Instant createdAt;
    private String snapshotJson;
    private boolean active;

    public ProjectBaselineDto() {
    }

    public ProjectBaselineDto(Long id, Long projectId, String name, Instant createdAt, String snapshotJson, boolean active) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.createdAt = createdAt;
        this.snapshotJson = snapshotJson;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }

    public boolean isActive() {
        return active;
    }
}
