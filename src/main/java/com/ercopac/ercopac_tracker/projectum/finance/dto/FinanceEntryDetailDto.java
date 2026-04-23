package com.ercopac.ercopac_tracker.projectum.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public class FinanceEntryDetailDto {
    private FinanceEntryDto row;
    private BigDecimal marginPercent;
    private Double percentCommitment;
    private Double percentForecast;
    private Double percentEac;
    private List<FinanceEntryDto> children;

    public FinanceEntryDto getRow() {
        return row;
    }

    public void setRow(FinanceEntryDto row) {
        this.row = row;
    }

    public BigDecimal getMarginPercent() {
        return marginPercent;
    }

    public void setMarginPercent(BigDecimal marginPercent) {
        this.marginPercent = marginPercent;
    }

    public Double getPercentCommitment() {
        return percentCommitment;
    }

    public void setPercentCommitment(Double percentCommitment) {
        this.percentCommitment = percentCommitment;
    }

    public Double getPercentForecast() {
        return percentForecast;
    }

    public void setPercentForecast(Double percentForecast) {
        this.percentForecast = percentForecast;
    }

    public Double getPercentEac() {
        return percentEac;
    }

    public void setPercentEac(Double percentEac) {
        this.percentEac = percentEac;
    }

    public List<FinanceEntryDto> getChildren() {
        return children;
    }

    public void setChildren(List<FinanceEntryDto> children) {
        this.children = children;
    }
}