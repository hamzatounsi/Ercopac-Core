package com.ercopac.ercopac_tracker.projectum.finance.dto;

import java.math.BigDecimal;

public class FinanceCostBreakdownDto {
    private BigDecimal actualCost;
    private BigDecimal forecast;
    private BigDecimal remainingBudget;

    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }

    public BigDecimal getForecast() { return forecast; }
    public void setForecast(BigDecimal forecast) { this.forecast = forecast; }

    public BigDecimal getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(BigDecimal remainingBudget) { this.remainingBudget = remainingBudget; }
}