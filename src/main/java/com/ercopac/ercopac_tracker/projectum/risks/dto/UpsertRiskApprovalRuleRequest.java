package com.ercopac.ercopac_tracker.projectum.risks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpsertRiskApprovalRuleRequest {

    @NotBlank
    private String riskLevel;

    @NotNull
    private Integer minRiskValue;

    @NotBlank
    private String approverRole;

    private Long approverUserId;

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Integer getMinRiskValue() { return minRiskValue; }
    public void setMinRiskValue(Integer minRiskValue) { this.minRiskValue = minRiskValue; }

    public String getApproverRole() { return approverRole; }
    public void setApproverRole(String approverRole) { this.approverRole = approverRole; }

    public Long getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Long approverUserId) { this.approverUserId = approverUserId; }
}