package com.ercopac.ercopac_tracker.integration.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "erp_projects")
public class ErpProject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "erp_project_code", nullable = false, unique = true)
    private String erpProjectCode;
    
    @Column(name = "project_name")
    private String projectName;
    
    @Column(name = "company_code")
    private String companyCode;
    
    @Column(name = "total_budget", precision = 15, scale = 2)
    private BigDecimal totalBudget = BigDecimal.ZERO;
    
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters et Setters
    public Long getId() { return id; }
    public String getErpProjectCode() { return erpProjectCode; }
    public void setErpProjectCode(String v) { erpProjectCode = v; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String v) { projectName = v; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String v) { companyCode = v; }
    public BigDecimal getTotalBudget() { return totalBudget; }
    public void setTotalBudget(BigDecimal v) { totalBudget = v; }
    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime v) { lastSyncedAt = v; }
}