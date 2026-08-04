package com.ercopac.ercopac_tracker.projectum.finance.dto;

import java.math.BigDecimal;

public class UpsertFinanceEntryRequest {
    private Long id;
    private String wbsCode;
    private String description;
    private Integer level;
    private BigDecimal sales;
    private BigDecimal budget;
    private BigDecimal commitment;
    private BigDecimal actualCost;
    private BigDecimal forecast;
    private String ownerName;
    
    // ✅ NEW: ERP fields
    private BigDecimal costReserve;
    private BigDecimal updatedBudget;

    // Getters and Setters for ALL fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getWbsCode() { return wbsCode; }
    public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    
    public BigDecimal getSales() { return sales; }
    public void setSales(BigDecimal sales) { this.sales = sales; }
    
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    
    public BigDecimal getCommitment() { return commitment; }
    public void setCommitment(BigDecimal commitment) { this.commitment = commitment; }
    
    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }
    
    public BigDecimal getForecast() { return forecast; }
    public void setForecast(BigDecimal forecast) { this.forecast = forecast; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    // ✅ NEW getters/setters
    public BigDecimal getCostReserve() { return costReserve; }
    public void setCostReserve(BigDecimal costReserve) { this.costReserve = costReserve; }
    
    public BigDecimal getUpdatedBudget() { return updatedBudget; }
    public void setUpdatedBudget(BigDecimal updatedBudget) { this.updatedBudget = updatedBudget; }
}