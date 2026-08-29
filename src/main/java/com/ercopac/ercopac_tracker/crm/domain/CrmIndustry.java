package com.ercopac.ercopac_tracker.crm.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_industries")
public class CrmIndustry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private Organisation organisation;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();
    @PreUpdate void updated() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation value) { organisation = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
