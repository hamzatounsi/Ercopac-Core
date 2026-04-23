package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FinanceSettingsDto {
    private BigDecimal defaultHourlyRate;
    private List<FinanceWbsTemplateRowDto> templateRows = new ArrayList<>();
    private List<FinanceOwnerMappingDto> ownerMappings = new ArrayList<>();
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