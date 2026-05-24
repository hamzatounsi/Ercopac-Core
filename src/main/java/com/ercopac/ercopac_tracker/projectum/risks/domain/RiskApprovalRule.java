package com.ercopac.ercopac_tracker.projectum.risks.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "risk_approval_rules")
public class RiskApprovalRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organisation_id", nullable = false)
    private Long organisationId;

    @Column(name = "project_id")
    private Long projectId; // null = org-wide rule

    @Column(nullable = false, length = 20)
    private String riskLevel; // low, med, hi, crit

    @Column(nullable = false)
    private Integer minRiskValue; // 0, 5, 10, 17

    @Column(length = 60)
    private String approverRole; // "Project Manager", "Senior Manager", "Director"

    @Column(name = "approver_user_id")
    private Long approverUserId; // specific person

    public RiskApprovalRule() {}

    public Long getId() { return id; }

    public Long getOrganisationId() { return organisationId; }
    public void setOrganisationId(Long organisationId) { this.organisationId = organisationId; }

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
}