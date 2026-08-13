package com.ercopac.ercopac_tracker.projectum.forecast.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ForecastRowDto {
    private Long financeEntryId;
    private String wbsCode;
    private String description;
    private Integer level;
    private String rowType;
    private BigDecimal budget;
    private BigDecimal actualCost;
    private BigDecimal totalForecast;
    
    // ✅ Proper fields for the calculation (No TODOs!)
    private String resourceTypeCode;
    private BigDecimal remainingHours;
    private BigDecimal remainingCost;
    
    private List<ForecastGridCellDto> periods = new ArrayList<>();

    // --- Getters and Setters ---
    public Long getFinanceEntryId() { return financeEntryId; }
    public void setFinanceEntryId(Long financeEntryId) { this.financeEntryId = financeEntryId; }

    public String getWbsCode() { return wbsCode; }
    public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getRowType() { return rowType; }
    public void setRowType(String rowType) { this.rowType = rowType; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }

    public BigDecimal getTotalForecast() { return totalForecast; }
    public void setTotalForecast(BigDecimal totalForecast) { this.totalForecast = totalForecast; }

    public String getResourceTypeCode() { return resourceTypeCode; }
    public void setResourceTypeCode(String resourceTypeCode) { this.resourceTypeCode = resourceTypeCode; }

    public BigDecimal getRemainingHours() { return remainingHours; }
    public void setRemainingHours(BigDecimal remainingHours) { this.remainingHours = remainingHours; }

    public BigDecimal getRemainingCost() { return remainingCost; }
    public void setRemainingCost(BigDecimal remainingCost) { this.remainingCost = remainingCost; }

    public List<ForecastGridCellDto> getPeriods() { return periods; }
    public void setPeriods(List<ForecastGridCellDto> periods) { this.periods = periods; }
}