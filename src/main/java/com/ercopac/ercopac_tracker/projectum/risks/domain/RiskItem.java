package com.ercopac.ercopac_tracker.projectum.risks.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.ResourceType;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "risk_items")
public class RiskItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 20)
    private String riskType; // risk | opportunity

    @Column(nullable = false, length = 20)
    private String state; // new | managing | closed | variance | cr

    @Column(nullable = false, length = 500)
    private String description;

    private LocalDate inputDate;
    private LocalDate dueDate;

    @Column(length = 1000)
    private String mitigation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type_id")
    private ResourceType resourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private AppUser ownerUser;    // replaces free text owner
    @Column(length = 120)
    private String wbsCode;

    @Column(nullable = false)
    private String impact = "1";    // free text

    @Column(nullable = false)
    private Integer probability = 10;  // 10-100

    @Column(length = 30)
    private String varianceStatus; // open | approved

    @Column(length = 120)
    private String approvedBy;

    private LocalDate approvedAt;

    @Column(length = 1000)
    private String notes;

    public RiskItem() {}
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }

    public AppUser getOwnerUser() { return ownerUser; }
    public void setOwnerUser(AppUser ownerUser) { this.ownerUser = ownerUser; }
    public Long getId() { return id; }

    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getRiskType() { return riskType; }
    public void setRiskType(String riskType) { this.riskType = riskType; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getInputDate() { return inputDate; }
    public void setInputDate(LocalDate inputDate) { this.inputDate = inputDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getMitigation() { return mitigation; }
    public void setMitigation(String mitigation) { this.mitigation = mitigation; }



    public String getWbsCode() { return wbsCode; }
    public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }

    public String getImpact() { return impact; }
    public void setImpact(String impact) { this.impact = impact; }

    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }

    public String getVarianceStatus() { return varianceStatus; }
    public void setVarianceStatus(String varianceStatus) { this.varianceStatus = varianceStatus; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDate getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDate approvedAt) { this.approvedAt = approvedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}