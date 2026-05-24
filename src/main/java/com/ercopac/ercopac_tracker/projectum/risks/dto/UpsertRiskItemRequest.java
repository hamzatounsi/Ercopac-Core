package com.ercopac.ercopac_tracker.projectum.risks.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class UpsertRiskItemRequest {

    @NotBlank
    private String riskType;

    @NotBlank
    private String state;

    @NotBlank
    private String description;

    private LocalDate inputDate;
    private LocalDate dueDate;
    private String mitigation;
 
    private String wbsCode;
 // ADD:
    private Long resourceTypeId;  // ← send the ID, not the code string
    private Long ownerUserId;
   
    private String impact;

   
    private Integer probability;

    private String varianceStatus;
    private String approvedBy;
    private LocalDate approvedAt;
    private String notes;

    public UpsertRiskItemRequest() {}

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

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

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