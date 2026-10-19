package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public class PaymentMilestoneDto {
	private Long id;
	private Integer milestoneOrder;
	private Integer percentage;
	private LocalDate dueDate;
	private String status;
	private BigDecimal calculatedValue; // Calculé côté backend : salesValue * percentage / 100

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Integer getMilestoneOrder() { return milestoneOrder; }
	public void setMilestoneOrder(Integer milestoneOrder) { this.milestoneOrder = milestoneOrder; }

	public Integer getPercentage() { return percentage; }
	public void setPercentage(Integer percentage) { this.percentage = percentage; }

	public LocalDate getDueDate() { return dueDate; }
	public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public BigDecimal getCalculatedValue() { return calculatedValue; }
	public void setCalculatedValue(BigDecimal calculatedValue) { this.calculatedValue = calculatedValue; }
}
