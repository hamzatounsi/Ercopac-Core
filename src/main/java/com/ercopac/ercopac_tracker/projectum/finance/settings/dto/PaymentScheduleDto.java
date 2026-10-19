package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
public class PaymentScheduleDto {
	private Long projectId;
	private String projectName;
	private BigDecimal salesValue;
	private String currency;
	private Integer totalPercentage;
	private List<PaymentMilestoneDto> milestones = new ArrayList<>();

	public Long getProjectId() { return projectId; }
	public void setProjectId(Long projectId) { this.projectId = projectId; }

	public String getProjectName() { return projectName; }
	public void setProjectName(String projectName) { this.projectName = projectName; }

	public BigDecimal getSalesValue() { return salesValue; }
	public void setSalesValue(BigDecimal salesValue) { this.salesValue = salesValue; }

	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }

	public Integer getTotalPercentage() { return totalPercentage; }
	public void setTotalPercentage(Integer totalPercentage) { this.totalPercentage = totalPercentage; }

	public List<PaymentMilestoneDto> getMilestones() { return milestones; }
	public void setMilestones(List<PaymentMilestoneDto> milestones) { this.milestones = milestones; }
}
