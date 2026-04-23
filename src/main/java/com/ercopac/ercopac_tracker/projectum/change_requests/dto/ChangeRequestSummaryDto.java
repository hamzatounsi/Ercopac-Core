package com.ercopac.ercopac_tracker.projectum.change_requests.dto;

import java.math.BigDecimal;

public class ChangeRequestSummaryDto {
    private long totalCount;
    private long openCount;
    private long submittedCount;
    private long acceptedCount;
    private long refusedCount;
    private long cancelledCount;
    private BigDecimal totalValue;
    private BigDecimal totalCost;
    private BigDecimal totalMargin;
    private BigDecimal totalMarginPercent;

    public ChangeRequestSummaryDto() {}

    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    public long getOpenCount() { return openCount; }
    public void setOpenCount(long openCount) { this.openCount = openCount; }

    public long getSubmittedCount() { return submittedCount; }
    public void setSubmittedCount(long submittedCount) { this.submittedCount = submittedCount; }

    public long getAcceptedCount() { return acceptedCount; }
    public void setAcceptedCount(long acceptedCount) { this.acceptedCount = acceptedCount; }

    public long getRefusedCount() { return refusedCount; }
    public void setRefusedCount(long refusedCount) { this.refusedCount = refusedCount; }

    public long getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(long cancelledCount) { this.cancelledCount = cancelledCount; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public BigDecimal getTotalMargin() { return totalMargin; }
    public void setTotalMargin(BigDecimal totalMargin) { this.totalMargin = totalMargin; }

    public BigDecimal getTotalMarginPercent() { return totalMarginPercent; }
    public void setTotalMarginPercent(BigDecimal totalMarginPercent) { this.totalMarginPercent = totalMarginPercent; }
}