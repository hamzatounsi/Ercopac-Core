package com.ercopac.ercopac_tracker.milestone.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "milestone_types")
public class MilestoneType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    // Nullable for schema compatibility with the former organisation-wide types.
    // New configuration records are always assigned to a project by the service.
    @Column(name = "project_id")
    private Long projectId;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, length = 20)
    private String color;

    @Column(name = "letter_code", length = 5)
    private String letterCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getLetterCode() { return letterCode; }
    public void setLetterCode(String letterCode) { this.letterCode = letterCode; }
    
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
