package com.ercopac.ercopac_tracker.crm.domain;

// Path: src/main/java/com/ercopac/ercopac_tracker/crm/domain/CrmOpportunity.java

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_opportunities")
public class CrmOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "account_name", length = 150)
    private String accountName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    private CrmPipelineStage stage;

    @Column(precision = 15, scale = 2)
    private BigDecimal value;

    @Column(nullable = false, length = 10)
    private String currency = "EUR";

    @Column(nullable = false)
    private Integer probability = 0;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private CrmLead lead;

    @Column(name = "is_won", nullable = false)
    private boolean won = false;

    @Column(name = "is_lost", nullable = false)
    private boolean lost = false;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public CrmOpportunity() {}

    public Long getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation o) { this.organisation = o; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public CrmPipelineStage getStage() { return stage; }
    public void setStage(CrmPipelineStage stage) { this.stage = stage; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }
    public LocalDate getClosingDate() { return closingDate; }
    public void setClosingDate(LocalDate closingDate) { this.closingDate = closingDate; }
    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser owner) { this.owner = owner; }
    public CrmLead getLead() { return lead; }
    public void setLead(CrmLead lead) { this.lead = lead; }
    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }
    public boolean isLost() { return lost; }
    public void setLost(boolean lost) { this.lost = lost; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}