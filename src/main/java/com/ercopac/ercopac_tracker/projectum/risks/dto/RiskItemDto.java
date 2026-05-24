package com.ercopac.ercopac_tracker.projectum.risks.dto;

import java.time.LocalDate;

public class RiskItemDto {
    private Long id;
    private String riskType;
    private String state;
    private String riskCode;      // ← ADD "R-001"
    private Long projectId;       // ← ADD
    private String projectCode; 
    private String description;
    private LocalDate inputDate;
    private LocalDate dueDate;
    private String mitigation;

    
    private String wbsCode;
    private String impact;
    private Integer probability;
    private Integer riskValue;
    private String riskLevel;
    private String varianceStatus;
    private String approvedBy;
    private LocalDate approvedAt;
    private String notes;
 // Resource Type fields
    private Long resourceTypeId;       // FK id → to send back on update
    private String resourceTypeCode;   // e.g. "ME" → for display
    private String resourceTypeLabel;  // e.g. "Mechanical" → for display
    private String resourceTypeColour; // e.g. "#3b82f6" → for color badge

    // Owner fields
    private Long ownerUserId;          // FK id → to send back on update
    private String ownerUserName;      // full name → for display
    private String ownerUserCode;      // employee code → for display  // employee code

    public RiskItemDto() {}
    public String getRiskCode() { return riskCode; }
    public void setRiskCode(String riskCode) { this.riskCode = riskCode; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Long getResourceTypeId() { return resourceTypeId; }
    public void setResourceTypeId(Long resourceTypeId) { this.resourceTypeId = resourceTypeId; }

    public String getResourceTypeCode() { return resourceTypeCode; }
    public void setResourceTypeCode(String resourceTypeCode) { this.resourceTypeCode = resourceTypeCode; }

    public String getResourceTypeLabel() { return resourceTypeLabel; }
    public void setResourceTypeLabel(String resourceTypeLabel) { this.resourceTypeLabel = resourceTypeLabel; }

    public String getResourceTypeColour() { return resourceTypeColour; }
    public void setResourceTypeColour(String resourceTypeColour) { this.resourceTypeColour = resourceTypeColour; }

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

    public String getOwnerUserName() { return ownerUserName; }
    public void setOwnerUserName(String ownerUserName) { this.ownerUserName = ownerUserName; }

    public String getOwnerUserCode() { return ownerUserCode; }
    public void setOwnerUserCode(String ownerUserCode) { this.ownerUserCode = ownerUserCode; }
    public String getWbsCode() { return wbsCode; }
    public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }

    public String getImpact() { return impact; }
    public void setImpact(String impact) { this.impact = impact; }

    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }

    public Integer getRiskValue() { return riskValue; }
    public void setRiskValue(Integer riskValue) { this.riskValue = riskValue; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getVarianceStatus() { return varianceStatus; }
    public void setVarianceStatus(String varianceStatus) { this.varianceStatus = varianceStatus; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDate getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDate approvedAt) { this.approvedAt = approvedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}