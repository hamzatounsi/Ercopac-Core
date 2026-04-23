package com.ercopac.ercopac_tracker.projectum.forecast.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class UpsertForecastEntryRequest {

    @NotBlank
    private String wbsCode;

    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}$")
    private String periodKey;

    @DecimalMin(value = "0.00")
    private BigDecimal amount;

    public UpsertForecastEntryRequest() {
    }

    public String getWbsCode() {
        return wbsCode;
    }

    public void setWbsCode(String wbsCode) {
        this.wbsCode = wbsCode;
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