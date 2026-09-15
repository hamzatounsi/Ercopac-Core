package com.ercopac.ercopac_tracker.crm.dto;

import java.math.BigDecimal;
import java.util.List;

public class SalesDashboardDto {
    private BigDecimal orderIntakeMtd;
    private BigDecimal pipelineValue;
    private Long openOpportunities;
    private Double winRate;
    private List<MonthlyRevenueDto> monthlyRevenue;
    private List<StageMetricDto> pipelineByStage;
    private List<CrmOpportunityDto> topOpportunities;

    // Getters et Setters
    public BigDecimal getOrderIntakeMtd() { return orderIntakeMtd; }
    public void setOrderIntakeMtd(BigDecimal orderIntakeMtd) { this.orderIntakeMtd = orderIntakeMtd; }
    public BigDecimal getPipelineValue() { return pipelineValue; }
    public void setPipelineValue(BigDecimal pipelineValue) { this.pipelineValue = pipelineValue; }
    public Long getOpenOpportunities() { return openOpportunities; }
    public void setOpenOpportunities(Long openOpportunities) { this.openOpportunities = openOpportunities; }
    public Double getWinRate() { return winRate; }
    public void setWinRate(Double winRate) { this.winRate = winRate; }
    public List<MonthlyRevenueDto> getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(List<MonthlyRevenueDto> monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
    public List<StageMetricDto> getPipelineByStage() { return pipelineByStage; }
    public void setPipelineByStage(List<StageMetricDto> pipelineByStage) { this.pipelineByStage = pipelineByStage; }
    public List<CrmOpportunityDto> getTopOpportunities() { return topOpportunities; }
    public void setTopOpportunities(List<CrmOpportunityDto> topOpportunities) { this.topOpportunities = topOpportunities; }

    public static class MonthlyRevenueDto {
        private String month;
        private BigDecimal target;
        private BigDecimal actual;
        public MonthlyRevenueDto(String month, BigDecimal target, BigDecimal actual) {
            this.month = month; this.target = target; this.actual = actual;
        }
        public String getMonth() { return month; }
        public BigDecimal getTarget() { return target; }
        public BigDecimal getActual() { return actual; }
    }

    public static class StageMetricDto {
        private String stageName;
        private String stageColor;
        private Long count;
        private BigDecimal value;
        public StageMetricDto(String stageName, String stageColor, Long count, BigDecimal value) {
            this.stageName = stageName; this.stageColor = stageColor; this.count = count; this.value = value;
        }
        public String getStageName() { return stageName; }
        public String getStageColor() { return stageColor; }
        public Long getCount() { return count; }
        public BigDecimal getValue() { return value; }
    }
}