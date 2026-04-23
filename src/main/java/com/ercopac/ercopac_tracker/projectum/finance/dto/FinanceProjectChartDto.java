package com.ercopac.ercopac_tracker.projectum.finance.dto;

import java.math.BigDecimal;

public class FinanceProjectChartDto {
    private Long projectId;
    private String projectCode;
    private String projectName;
    private BigDecimal budget;
    private BigDecimal eac;
    private BigDecimal sales;
    private BigDecimal actualCost;
    private BigDecimal forecast;
    private BigDecimal variance;
    private BigDecimal marginPercent;
    public Long getProjectId() {
        return projectId;
    }
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    public String getProjectCode() {
        return projectCode;
    }
    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }
    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public BigDecimal getBudget() {
        return budget;
    }
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
    public BigDecimal getEac() {
        return eac;
    }
    public void setEac(BigDecimal eac) {
        this.eac = eac;
    }
    public BigDecimal getSales() {
        return sales;
    }
    public void setSales(BigDecimal sales) {
        this.sales = sales;
    }
    public BigDecimal getActualCost() {
        return actualCost;
    }
    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }
    public BigDecimal getForecast() {
        return forecast;
    }
    public void setForecast(BigDecimal forecast) {
        this.forecast = forecast;
    }
    public BigDecimal getVariance() {
        return variance;
    }
    public void setVariance(BigDecimal variance) {
        this.variance = variance;
    }
    public BigDecimal getMarginPercent() {
        return marginPercent;
    }
    public void setMarginPercent(BigDecimal marginPercent) {
        this.marginPercent = marginPercent;
    }

    // getters/setters

    
}