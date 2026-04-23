package com.ercopac.ercopac_tracker.projectum.forecast.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ForecastRowDto {
    private String wbsCode;
    private String description;
    private Integer level;
    private BigDecimal budget;
    private BigDecimal actualCost;
    private BigDecimal totalForecast;
    private List<ForecastGridCellDto> periods = new ArrayList<>();

    public ForecastRowDto() {
    }

    public String getWbsCode() {
        return wbsCode;
    }

    public void setWbsCode(String wbsCode) {
        this.wbsCode = wbsCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }

    public BigDecimal getTotalForecast() {
        return totalForecast;
    }

    public void setTotalForecast(BigDecimal totalForecast) {
        this.totalForecast = totalForecast;
    }

    public List<ForecastGridCellDto> getPeriods() {
        return periods;
    }

    public void setPeriods(List<ForecastGridCellDto> periods) {
        this.periods = periods;
    }
}