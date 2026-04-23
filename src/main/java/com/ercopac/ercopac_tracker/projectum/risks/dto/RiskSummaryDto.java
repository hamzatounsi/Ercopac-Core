package com.ercopac.ercopac_tracker.projectum.risks.dto;

public class RiskSummaryDto {
    private long total;
    private long critical;
    private long high;
    private long medium;
    private long low;
    private long openRisks;
    private long pendingVariance;
    private long opportunityCount;
    private long riskCount;
    private long netExposureScore;

    public RiskSummaryDto() {}

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getCritical() { return critical; }
    public void setCritical(long critical) { this.critical = critical; }

    public long getHigh() { return high; }
    public void setHigh(long high) { this.high = high; }

    public long getMedium() { return medium; }
    public void setMedium(long medium) { this.medium = medium; }

    public long getLow() { return low; }
    public void setLow(long low) { this.low = low; }

    public long getOpenRisks() { return openRisks; }
    public void setOpenRisks(long openRisks) { this.openRisks = openRisks; }

    public long getPendingVariance() { return pendingVariance; }
    public void setPendingVariance(long pendingVariance) { this.pendingVariance = pendingVariance; }

    public long getOpportunityCount() { return opportunityCount; }
    public void setOpportunityCount(long opportunityCount) { this.opportunityCount = opportunityCount; }

    public long getRiskCount() { return riskCount; }
    public void setRiskCount(long riskCount) { this.riskCount = riskCount; }

    public long getNetExposureScore() { return netExposureScore; }
    public void setNetExposureScore(long netExposureScore) { this.netExposureScore = netExposureScore; }
}