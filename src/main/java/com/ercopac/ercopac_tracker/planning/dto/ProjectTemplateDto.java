package com.ercopac.ercopac_tracker.planning.dto;

import java.time.Instant;

public class ProjectTemplateDto {

    private Long id;
    private Long projectId;
    private String name;
    private String scope;
    private String description;
    private Instant createdAt;
    private String snapshotJson;

    public ProjectTemplateDto() {
    }

    public ProjectTemplateDto(
            Long id,
            Long projectId,
            String name,
            String scope,
            String description,
            Instant createdAt,
            String snapshotJson
    ) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.scope = scope;
        this.description = description;
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

    public String getScope() {
        return scope;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }
}