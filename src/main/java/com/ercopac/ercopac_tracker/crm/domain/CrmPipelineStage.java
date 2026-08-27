package com.ercopac.ercopac_tracker.crm.domain;

// Path: src/main/java/com/ercopac/ercopac_tracker/crm/domain/CrmPipelineStage.java

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_pipeline_stages",
       uniqueConstraints = @UniqueConstraint(columnNames = {"organisation_id", "name"}))
public class CrmPipelineStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String color = "#64748b";

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    // The database has pre-supervisor-template installations with populated
    // pipeline rows.  A database default lets Hibernate add this field without
    // failing while PostgreSQL backfills those rows during an in-place upgrade.
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer probability = 0;

    @Column(name = "is_won", nullable = false)
    private boolean won = false;

    @Column(name = "is_lost", nullable = false)
    private boolean lost = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public CrmPipelineStage() {}

    public CrmPipelineStage(Organisation org, String name, String color, int order, int probability, boolean won, boolean lost) {
        this.organisation = org;
        this.name = name;
        this.color = color;
        this.displayOrder = order;
        this.probability = probability;
        this.won = won;
        this.lost = lost;
    }

    public Long getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation o) { this.organisation = o; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }
    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }
    public boolean isLost() { return lost; }
    public void setLost(boolean lost) { this.lost = lost; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
