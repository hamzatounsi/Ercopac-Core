package com.ercopac.ercopac_tracker.projectum.change_requests.dto;

import java.time.LocalDateTime;

public class ChangeRequestHistoryDto {
    private Long id;
    private String action;
    private String performedBy;
    private LocalDateTime createdAt;

    public ChangeRequestHistoryDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}