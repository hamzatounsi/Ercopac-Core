package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;

import java.math.BigDecimal;

public class FinanceHourlyRateDto {
    private Long id;
    private String resourceType;
    private BigDecimal hourlyRate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
}