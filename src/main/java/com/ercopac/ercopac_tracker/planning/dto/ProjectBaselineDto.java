package com.ercopac.ercopac_tracker.planning.dto;

import java.time.Instant;

public class ProjectBaselineDto {

    private Long id;
    private Long projectId;
    private String name;
    private Instant createdAt;
    private String snapshotJson;

    public ProjectBaselineDto() {
    }

    public ProjectBaselineDto(Long id, Long projectId, String name, Instant createdAt, String snapshotJson) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.createdAt = createdAt;
        this.snapshotJson = snapshotJson;
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
}