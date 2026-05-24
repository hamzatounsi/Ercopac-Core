package com.ercopac.ercopac_tracker.projectum.risks.dto;

public class RiskApprovalRuleDto {
    private Long id;
    private Long projectId;
    private String riskLevel;
    private Integer minRiskValue;
    private String approverRole;
    private Long approverUserId;
    private String approverUserName; // populated from user lookup

    public RiskApprovalRuleDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Integer getMinRiskValue() { return minRiskValue; }
    public void setMinRiskValue(Integer minRiskValue) { this.minRiskValue = minRiskValue; }

    public String getApproverRole() { return approverRole; }
    public void setApproverRole(String approverRole) { this.approverRole = approverRole; }

    public Long getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Long approverUserId) { this.approverUserId = approverUserId; }

    public String getApproverUserName() { return approverUserName; }
    public void setApproverUserName(String approverUserName) { this.approverUserName = approverUserName; }
}