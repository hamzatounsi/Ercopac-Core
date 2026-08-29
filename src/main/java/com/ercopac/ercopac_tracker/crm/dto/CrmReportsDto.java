package com.ercopac.ercopac_tracker.crm.dto;

import java.math.BigDecimal;
import java.util.List;

public record CrmReportsDto(
        long totalOpportunities,
        BigDecimal totalValue,
        BigDecimal weightedValue,
        List<Breakdown> byCountry,
        List<Breakdown> byStage,
        List<Breakdown> bySupplyCategory,
        BigDecimal materialValue,
        BigDecimal servicesValue,
        List<CrmOpportunityDto> opportunities
) {
    public record Breakdown(String key, long count, BigDecimal value) {}
}
