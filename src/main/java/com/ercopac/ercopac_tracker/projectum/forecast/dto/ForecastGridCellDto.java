package com.ercopac.ercopac_tracker.projectum.forecast.dto;

import java.math.BigDecimal;

public class ForecastGridCellDto {
    private String periodKey;
    private BigDecimal amount;

    public ForecastGridCellDto() {
    }

    public ForecastGridCellDto(String periodKey, BigDecimal amount) {
        this.periodKey = periodKey;
        this.amount = amount;
    }

    public String getPeriodKey() {
        return periodKey;
    }

    public void setPeriodKey(String periodKey) {
        this.periodKey = periodKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}