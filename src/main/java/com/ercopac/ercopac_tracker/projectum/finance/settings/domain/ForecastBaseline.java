package com.ercopac.ercopac_tracker.projectum.finance.settings.domain;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "forecast_baseline")
public class ForecastBaseline {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organisation_id", nullable = false)
	private Organisation organisation;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(name = "wbs_code", nullable = false, length = 100)
	private String wbsCode;

	@Column(name = "description", length = 255)
	private String description;

	@Column(name = "level_no", nullable = false)
	private Integer level;

	@Column(name = "baseline_forecast", precision = 18, scale = 2)
	private BigDecimal baselineForecast;

	@Column(name = "baseline_date")
	private LocalDateTime baselineDate;

	// Getters and Setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Organisation getOrganisation() { return organisation; }
	public void setOrganisation(Organisation organisation) { this.organisation = organisation; }

	public Project getProject() { return project; }
	public void setProject(Project project) { this.project = project; }

	public String getWbsCode() { return wbsCode; }
	public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public Integer getLevel() { return level; }
	public void setLevel(Integer level) { this.level = level; }

	public BigDecimal getBaselineForecast() { return baselineForecast; }
	public void setBaselineForecast(BigDecimal baselineForecast) { this.baselineForecast = baselineForecast; }

	public LocalDateTime getBaselineDate() { return baselineDate; }
	public void setBaselineDate(LocalDateTime baselineDate) { this.baselineDate = baselineDate; }
}
