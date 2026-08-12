package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FinanceSettingsDto {
    private BigDecimal defaultHourlyRate;
    private List<FinanceWbsTemplateRowDto> templateRows = new ArrayList<>();
    

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



}