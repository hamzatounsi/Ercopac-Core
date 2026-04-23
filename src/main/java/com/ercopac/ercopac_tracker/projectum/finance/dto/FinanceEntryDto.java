package com.ercopac.ercopac_tracker.projectum.finance.dto;

import java.math.BigDecimal;

public class FinanceEntryDto {
    private Long id;
    private String wbsCode;
    private String description;
    private Integer level;
    private BigDecimal sales;
    private BigDecimal budget;
    private BigDecimal commitment;
    private BigDecimal actualCost;
    private BigDecimal forecast;
    private BigDecimal eac;
    private BigDecimal variance;
    private String owner;
    private Double cpi;
    private Double percentAc;

    public FinanceEntryDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getSales() {
        return sales;
    }

    public void setSales(BigDecimal sales) {
        this.sales = sales;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getCommitment() {
        return commitment;
    }

    public void setCommitment(BigDecimal commitment) {
        this.commitment = commitment;
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

    public BigDecimal getEac() {
        return eac;
    }

    public void setEac(BigDecimal eac) {
        this.eac = eac;
    }

    public BigDecimal getVariance() {
        return variance;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Double getCpi() {
        return cpi;
    }

    public void setCpi(Double cpi) {
        this.cpi = cpi;
    }

    public Double getPercentAc() {
        return percentAc;
    }

    public void setPercentAc(Double percentAc) {
        this.percentAc = percentAc;
    }

    public void setVariance(BigDecimal variance) {
        this.variance = variance;
    }
}