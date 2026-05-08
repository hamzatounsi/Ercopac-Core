package com.ercopac.ercopac_tracker.crm.dto;

// Path: src/main/java/com/ercopac/ercopac_tracker/crm/dto/CrmDashboardDto.java

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CrmDashboardDto {

    private long openOpportunities;
    private BigDecimal pipelineValue;
    private long activeLeads;
    private long wonThisMonth;
    private List<CrmActivityDto> recentActivities;
    private List<CrmOpportunityDto> closingThisMonth;
    private Map<String, Long> leadsBySource;
    private List<CrmPipelineStageDto> pipeline;

    public CrmDashboardDto() {}

    public long getOpenOpportunities() { return openOpportunities; }
    public void setOpenOpportunities(long openOpportunities) {
        this.openOpportunities = openOpportunities;
    }

    public BigDecimal getPipelineValue() { return pipelineValue; }
    public void setPipelineValue(BigDecimal pipelineValue) {
        this.pipelineValue = pipelineValue;
    }

    public long getActiveLeads() { return activeLeads; }
    public void setActiveLeads(long activeLeads) {
        this.activeLeads = activeLeads;
    }

    public long getWonThisMonth() { return wonThisMonth; }
    public void setWonThisMonth(long wonThisMonth) {
        this.wonThisMonth = wonThisMonth;
    }

    public List<CrmActivityDto> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<CrmActivityDto> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public List<CrmOpportunityDto> getClosingThisMonth() { return closingThisMonth; }
    public void setClosingThisMonth(List<CrmOpportunityDto> closingThisMonth) {
        this.closingThisMonth = closingThisMonth;
    }

    public Map<String, Long> getLeadsBySource() { return leadsBySource; }
    public void setLeadsBySource(Map<String, Long> leadsBySource) {
        this.leadsBySource = leadsBySource;
    }

    public List<CrmPipelineStageDto> getPipeline() { return pipeline; }
    public void setPipeline(List<CrmPipelineStageDto> pipeline) {
        this.pipeline = pipeline;
    }
}