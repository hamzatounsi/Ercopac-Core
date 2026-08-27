package com.ercopac.ercopac_tracker.crm.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "crm_opportunity_stage_history")
public class CrmOpportunityStageHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private Organisation organisation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "opportunity_id", nullable = false) private CrmOpportunity opportunity;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "stage_id") private CrmPipelineStage stage;
    @Column(name = "stage_name", nullable = false, length = 100) private String stageName;
    @Column(nullable = false) private Integer probability;
    @Column(name = "closing_date") private LocalDate closingDate;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "modified_by_id") private AppUser modifiedBy;
    @Column(name = "entered_at", nullable = false, updatable = false) private LocalDateTime enteredAt = LocalDateTime.now();
    public Long getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation value) { organisation = value; }
    public CrmOpportunity getOpportunity() { return opportunity; }
    public void setOpportunity(CrmOpportunity value) { opportunity = value; }
    public CrmPipelineStage getStage() { return stage; }
    public void setStage(CrmPipelineStage value) { stage = value; }
    public String getStageName() { return stageName; }
    public void setStageName(String value) { stageName = value; }
    public Integer getProbability() { return probability; }
    public void setProbability(Integer value) { probability = value; }
    public LocalDate getClosingDate() { return closingDate; }
    public void setClosingDate(LocalDate value) { closingDate = value; }
    public AppUser getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(AppUser value) { modifiedBy = value; }
    public LocalDateTime getEnteredAt() { return enteredAt; }
}
