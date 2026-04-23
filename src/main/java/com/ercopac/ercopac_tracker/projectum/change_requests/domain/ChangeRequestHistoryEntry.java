package com.ercopac.ercopac_tracker.projectum.change_requests.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "change_request_history")
public class ChangeRequestHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "change_request_id", nullable = false)
    private ChangeRequest changeRequest;

    @Column(nullable = false, length = 500)
    private String action;

    @Column(length = 120)
    private String performedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ChangeRequestHistoryEntry() {}

    public Long getId() { return id; }

    public ChangeRequest getChangeRequest() { return changeRequest; }
    public void setChangeRequest(ChangeRequest changeRequest) { this.changeRequest = changeRequest; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}