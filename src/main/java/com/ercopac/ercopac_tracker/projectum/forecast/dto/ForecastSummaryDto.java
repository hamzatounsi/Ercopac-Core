package com.ercopac.ercopac_tracker.projectum.forecast.dto;

import java.math.BigDecimal;

public class ForecastSummaryDto {
    private BigDecimal totalForecast;
    private BigDecimal totalBudget;
    private BigDecimal totalActualCost;
    private BigDecimal totalEac;
    private BigDecimal totalVariance;

    public ForecastSummaryDto() {
    }

    public BigDecimal getTotalForecast() {
        return totalForecast;
    }

    public void setTotalForecast(BigDecimal totalForecast) {
        this.totalForecast = totalForecast;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public BigDecimal getTotalActualCost() {
        return totalActualCost;
    }

    public void setTotalActualCost(BigDecimal totalActualCost) {
        this.totalActualCost = totalActualCost;
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