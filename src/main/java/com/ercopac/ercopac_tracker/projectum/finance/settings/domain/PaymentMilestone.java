package com.ercopac.ercopac_tracker.projectum.finance.settings.domain;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name = "payment_milestone")
public class PaymentMilestone {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organisation_id", nullable = false)
	private Organisation organisation;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(name = "sales_value", precision = 18, scale = 2)
	private BigDecimal salesValue;

	@Column(name = "currency", length = 10)
	private String currency;

	@Column(name = "milestone_order", nullable = false)
	private Integer milestoneOrder;

	@Column(name = "percentage", nullable = false)
	private Integer percentage;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Column(name = "status", length = 20, nullable = false)
	private String status; // 'PAID' or 'EXPECTED'

	// --- Getters and Setters ---
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Organisation getOrganisation() { return organisation; }
	public void setOrganisation(Organisation organisation) { this.organisation = organisation; }

	public Project getProject() { return project; }
	public void setProject(Project project) { this.project = project; }

	public BigDecimal getSalesValue() { return salesValue; }
	public void setSalesValue(BigDecimal salesValue) { this.salesValue = salesValue; }

	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }

	public Integer getMilestoneOrder() { return milestoneOrder; }
	public void setMilestoneOrder(Integer milestoneOrder) { this.milestoneOrder = milestoneOrder; }

	public Integer getPercentage() { return percentage; }
	public void setPercentage(Integer percentage) { this.percentage = percentage; }

	public LocalDate getDueDate() { return dueDate; }
	public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
}
