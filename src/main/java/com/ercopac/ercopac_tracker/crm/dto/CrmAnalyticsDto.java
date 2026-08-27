package com.ercopac.ercopac_tracker.crm.dto;

import java.math.BigDecimal;
import java.util.List;

/** Read-only, tenant-scoped data used by the CRM Analytics workspace. */
public record CrmAnalyticsDto(
        String opportunityType,
        long totalOpportunities,
        BigDecimal pipelineValue,
        BigDecimal wonValue,
        long activeLeads,
        List<StageMetric> pipelineByStage,
        List<SourceMetric> leadsBySource,
        List<CrmOpportunityDto> opportunities
) {
    public record StageMetric(String name, String color, long count, BigDecimal value) {}
    public record SourceMetric(String name, long count) {}
}
