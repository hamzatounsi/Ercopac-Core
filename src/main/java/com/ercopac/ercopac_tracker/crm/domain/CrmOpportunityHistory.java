package com.ercopac.ercopac_tracker.crm.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "crm_opportunity_history")
public class CrmOpportunityHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private Organisation organisation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "opportunity_id", nullable = false) private CrmOpportunity opportunity;
    @Column(name = "field_name", nullable = false, length = 80) private String fieldName;
    @Column(name = "old_value", length = 2000) private String oldValue;
    @Column(name = "new_value", length = 2000) private String newValue;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "changed_by_id") private AppUser changedBy;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation value) { organisation = value; }
    public CrmOpportunity getOpportunity() { return opportunity; }
    public void setOpportunity(CrmOpportunity value) { opportunity = value; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String value) { fieldName = value; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String value) { oldValue = value; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String value) { newValue = value; }
    public AppUser getChangedBy() { return changedBy; }
    public void setChangedBy(AppUser value) { changedBy = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
