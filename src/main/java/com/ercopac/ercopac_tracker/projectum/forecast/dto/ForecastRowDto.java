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
    
    private String resourceTypeCode;
    private BigDecimal remainingHours;
    private BigDecimal remainingCost;
    
    // ✅ NOUVEAU : Code WBS du Schedule lié manuellement
    private String linkedScheduleWbs;
    
    // ✅ NOUVEAU : Liste des tâches Schedule disponibles pour le dropdown
    private List<ScheduleTaskOption> availableScheduleTasks;
    
    private List<ForecastGridCellDto> periods = new ArrayList<>();

    // Getters et Setters existants
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

    // ✅ NOUVEAUX GETTERS/SETTERS
    public String getLinkedScheduleWbs() { return linkedScheduleWbs; }
    public void setLinkedScheduleWbs(String linkedScheduleWbs) { this.linkedScheduleWbs = linkedScheduleWbs; }

    public List<ScheduleTaskOption> getAvailableScheduleTasks() { return availableScheduleTasks; }
    public void setAvailableScheduleTasks(List<ScheduleTaskOption> availableScheduleTasks) { this.availableScheduleTasks = availableScheduleTasks; }

    // ✅ Classe interne pour les options du dropdown
    public static class ScheduleTaskOption {
        private String wbsCode;
        private String name;
        private Integer outlineLevel;
        private BigDecimal plannedHours;

        public String getWbsCode() { return wbsCode; }
        public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getOutlineLevel() { return outlineLevel; }
        public void setOutlineLevel(Integer outlineLevel) { this.outlineLevel = outlineLevel; }

        public BigDecimal getPlannedHours() { return plannedHours; }
        public void setPlannedHours(BigDecimal plannedHours) { this.plannedHours = plannedHours; }
    }
}