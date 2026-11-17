package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public class ForecastBaselineDto {
	private Long projectId;
	private String projectName;
	private boolean hasBaseline;
	private LocalDateTime baselineDate;
	private BigDecimal totalBaselineForecast;
	private BigDecimal totalCurrentForecast;
	private BigDecimal totalVariance;
	private List<ForecastBaselineRowDto> rows = new ArrayList<>();

	// Getters and Setters
	public Long getProjectId() { return projectId; }
	public void setProjectId(Long projectId) { this.projectId = projectId; }

	public String getProjectName() { return projectName; }
	public void setProjectName(String projectName) { this.projectName = projectName; }

	public boolean isHasBaseline() { return hasBaseline; }
	public void setHasBaseline(boolean hasBaseline) { this.hasBaseline = hasBaseline; }

	public LocalDateTime getBaselineDate() { return baselineDate; }
	public void setBaselineDate(LocalDateTime baselineDate) { this.baselineDate = baselineDate; }

	public BigDecimal getTotalBaselineForecast() { return totalBaselineForecast; }
	public void setTotalBaselineForecast(BigDecimal totalBaselineForecast) { this.totalBaselineForecast = totalBaselineForecast; }

	public BigDecimal getTotalCurrentForecast() { return totalCurrentForecast; }
	public void setTotalCurrentForecast(BigDecimal totalCurrentForecast) { this.totalCurrentForecast = totalCurrentForecast; }

	public BigDecimal getTotalVariance() { return totalVariance; }
	public void setTotalVariance(BigDecimal totalVariance) { this.totalVariance = totalVariance; }

	public List<ForecastBaselineRowDto> getRows() { return rows; }
	public void setRows(List<ForecastBaselineRowDto> rows) { this.rows = rows; }
}
