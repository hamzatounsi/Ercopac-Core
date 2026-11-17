package com.ercopac.ercopac_tracker.integration.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "erp_wbs_items")
public class ErpWbsItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "erp_item_id", nullable = false, unique = true)
    private Long erpItemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ErpProject project;
    
    @Column(name = "wbs_code", nullable = false)
    private String wbsCode;
    
    @Column(name = "parent_wbs_code")
    private String parentWbsCode;
    
    @Column(name = "wbs_level")
    private Integer wbsLevel;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "item_type")
    private String itemType;
    
    @Column(length = 100)
    private String department;
    
    @Column(name = "sequence_order")
    private Integer sequenceOrder;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal commitment = BigDecimal.ZERO;
    
    @Column(name = "actual_cost", precision = 15, scale = 2)
    private BigDecimal actualCost = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal forecast = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal eac = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal variance = BigDecimal.ZERO;
    
    @Column(length = 3)
    private String currency = "EUR";
    
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }
    
    // Getters et Setters
    public Long getId() { return id; }
    public Long getErpItemId() { return erpItemId; }
    public void setErpItemId(Long v) { erpItemId = v; }
    public ErpProject getProject() { return project; }
    public void setProject(ErpProject v) { project = v; }
    public String getWbsCode() { return wbsCode; }
    public void setWbsCode(String v) { wbsCode = v; }
    public String getParentWbsCode() { return parentWbsCode; }
    public void setParentWbsCode(String v) { parentWbsCode = v; }
    public Integer getWbsLevel() { return wbsLevel; }
    public void setWbsLevel(Integer v) { wbsLevel = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getItemType() { return itemType; }
    public void setItemType(String v) { itemType = v; }
    public String getDepartment() { return department; }
    public void setDepartment(String v) { department = v; }
    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer v) { sequenceOrder = v; }
    public BigDecimal getCommitment() { return commitment; }
    public void setCommitment(BigDecimal v) { commitment = v; }
    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal v) { actualCost = v; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal v) { quantity = v; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal v) { budget = v; }
    public BigDecimal getForecast() { return forecast; }
    public void setForecast(BigDecimal v) { forecast = v; }
    public BigDecimal getEac() { return eac; }
    public void setEac(BigDecimal v) { eac = v; }
    public BigDecimal getVariance() { return variance; }
    public void setVariance(BigDecimal v) { variance = v; }
    public String getCurrency() { return currency; }
    public void setCurrency(String v) { currency = v; }
    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime v) { lastSyncedAt = v; }
}