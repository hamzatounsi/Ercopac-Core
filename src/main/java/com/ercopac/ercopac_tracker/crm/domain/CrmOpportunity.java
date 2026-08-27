package com.ercopac.ercopac_tracker.crm.domain;

// Path: src/main/java/com/ercopac/ercopac_tracker/crm/domain/CrmOpportunity.java

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

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
    @JoinColumn(name = "account_id")
    private CrmAccount account;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "crm_opportunity_team",
            joinColumns = @JoinColumn(name = "opportunity_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<AppUser> teamMembers = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_category_id")
    private CrmSupplyCategory supplyCategory;

    @Column(name = "opportunity_type", length = 20)
    private String opportunityType;

    @Column(length = 20)
    private String pipeline;

    @Column(name = "quote_number", length = 80)
    private String quoteNumber;

    @Column(name = "quote_requested_date")
    private LocalDate quoteRequestedDate;

    @Column(name = "quote_submitted_date")
    private LocalDate quoteSubmittedDate;

    @Column(name = "shipment_date")
    private LocalDate shipmentDate;

    @Column(name = "next_step", length = 500)
    private String nextStep;

    @Column(length = 8000)
    private String description;

    @Column(name = "material_value", precision = 15, scale = 2)
    private BigDecimal materialValue;

    @Column(name = "services_value", precision = 15, scale = 2)
    private BigDecimal servicesValue;

    @Column(name = "ercopac_material_value", precision = 15, scale = 2)
    private BigDecimal ercopacMaterialValue;

    @Column(name = "third_party_material_value", precision = 15, scale = 2)
    private BigDecimal thirdPartyMaterialValue;

    @Column(name = "ercopac_resale_value", precision = 15, scale = 2)
    private BigDecimal ercopacResaleValue;

    @Column(name = "resale_value", precision = 15, scale = 2)
    private BigDecimal resaleValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

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
    public CrmAccount getAccount() { return account; }
    public void setAccount(CrmAccount value) { account = value; }
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
    public Set<AppUser> getTeamMembers() { return teamMembers; }
    public void setTeamMembers(Set<AppUser> teamMembers) { this.teamMembers = teamMembers; }
    public CrmSupplyCategory getSupplyCategory() { return supplyCategory; }
    public void setSupplyCategory(CrmSupplyCategory value) { supplyCategory = value; }
    public String getOpportunityType() { return opportunityType; }
    public void setOpportunityType(String value) { opportunityType = value; }
    public String getPipeline() { return pipeline; }
    public void setPipeline(String value) { pipeline = value; }
    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String value) { quoteNumber = value; }
    public LocalDate getQuoteRequestedDate() { return quoteRequestedDate; }
    public void setQuoteRequestedDate(LocalDate value) { quoteRequestedDate = value; }
    public LocalDate getQuoteSubmittedDate() { return quoteSubmittedDate; }
    public void setQuoteSubmittedDate(LocalDate value) { quoteSubmittedDate = value; }
    public LocalDate getShipmentDate() { return shipmentDate; }
    public void setShipmentDate(LocalDate value) { shipmentDate = value; }
    public String getNextStep() { return nextStep; }
    public void setNextStep(String value) { nextStep = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public BigDecimal getMaterialValue() { return materialValue; }
    public void setMaterialValue(BigDecimal value) { materialValue = value; }
    public BigDecimal getServicesValue() { return servicesValue; }
    public void setServicesValue(BigDecimal value) { servicesValue = value; }
    public BigDecimal getErcopacMaterialValue() { return ercopacMaterialValue; }
    public void setErcopacMaterialValue(BigDecimal value) { ercopacMaterialValue = value; }
    public BigDecimal getThirdPartyMaterialValue() { return thirdPartyMaterialValue; }
    public void setThirdPartyMaterialValue(BigDecimal value) { thirdPartyMaterialValue = value; }
    public BigDecimal getErcopacResaleValue() { return ercopacResaleValue; }
    public void setErcopacResaleValue(BigDecimal value) { ercopacResaleValue = value; }
    public BigDecimal getResaleValue() { return resaleValue; }
    public void setResaleValue(BigDecimal value) { resaleValue = value; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }
    public boolean isLost() { return lost; }
    public void setLost(boolean lost) { this.lost = lost; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
