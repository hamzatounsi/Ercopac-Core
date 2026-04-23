package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaveFinanceSettingsRequest {

    @NotNull
    private BigDecimal defaultHourlyRate;

    @Valid
    private List<FinanceWbsTemplateRowDto> templateRows = new ArrayList<>();

    @Valid
    private List<FinanceOwnerMappingDto> ownerMappings = new ArrayList<>();

    @Valid
    private List<FinanceHourlyRateDto> hourlyRates = new ArrayList<>();

    public BigDecimal getDefaultHourlyRate() {
        return defaultHourlyRate;
    }

    public void setDefaultHourlyRate(BigDecimal defaultHourlyRate) {
        this.defaultHourlyRate = defaultHourlyRate;
    }

    public List<FinanceWbsTemplateRowDto> getTemplateRows() {
        return templateRows;
    }

    public void setTemplateRows(List<FinanceWbsTemplateRowDto> templateRows) {
        this.templateRows = templateRows;
    }

    public List<FinanceOwnerMappingDto> getOwnerMappings() {
        return ownerMappings;
    }

    public void setOwnerMappings(List<FinanceOwnerMappingDto> ownerMappings) {
        this.ownerMappings = ownerMappings;
    }

    public List<FinanceHourlyRateDto> getHourlyRates() {
        return hourlyRates;
    }

    public void setHourlyRates(List<FinanceHourlyRateDto> hourlyRates) {
        this.hourlyRates = hourlyRates;
    }
}