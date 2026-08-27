package com.ercopac.ercopac_tracker.crm.dto;

import java.math.BigDecimal;
import java.util.List;

public record CrmManagerViewDto(List<TeamMember> team, List<CrmOpportunityDto> opportunities, int year) {
    public record TeamMember(Long userId, String name, String role, long opportunityCount,
                             BigDecimal pipelineValue, BigDecimal wonValue,
                             BigDecimal target, String currency) {}
}
