package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;
import java.math.BigDecimal;
import java.util.List;
public class SavePaymentScheduleRequest {
	private BigDecimal salesValue;
	private String currency;
	private List<PaymentMilestoneDto> milestones;

	public BigDecimal getSalesValue() { return salesValue; }
	public void setSalesValue(BigDecimal salesValue) { this.salesValue = salesValue; }

	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }

	public List<PaymentMilestoneDto> getMilestones() { return milestones; }
	public void setMilestones(List<PaymentMilestoneDto> milestones) { this.milestones = milestones; }
}
