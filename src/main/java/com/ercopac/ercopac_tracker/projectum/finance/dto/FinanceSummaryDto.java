package com.ercopac.ercopac_tracker.projectum.finance.dto;

import java.math.BigDecimal;

public class FinanceSummaryDto {
    private BigDecimal totalSales;
    private BigDecimal totalBudget;
    private BigDecimal totalCommitment;
    private BigDecimal totalActualCost;
    private BigDecimal totalForecast;
    private BigDecimal totalEac;
    private BigDecimal totalVariance;

    public FinanceSummaryDto() {
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public BigDecimal getTotalCommitment() {
        return totalCommitment;
    }

    public void setTotalCommitment(BigDecimal totalCommitment) {
        this.totalCommitment = totalCommitment;
    }

    public BigDecimal getTotalActualCost() {
        return totalActualCost;
    }

    public void setTotalActualCost(BigDecimal totalActualCost) {
        this.totalActualCost = totalActualCost;
    }

    public BigDecimal getTotalForecast() {
        return totalForecast;
    }

    public void setTotalForecast(BigDecimal totalForecast) {
        this.totalForecast = totalForecast;
    }

    public BigDecimal getTotalEac() {
        return totalEac;
    }

    public void setTotalEac(BigDecimal totalEac) {
        this.totalEac = totalEac;
    }

    public BigDecimal getTotalVariance() {
        return totalVariance;
    }

    public void setTotalVariance(BigDecimal totalVariance) {
        this.totalVariance = totalVariance;
    }
}