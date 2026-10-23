package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class ForecastBaselineRowDto {
	private String wbsCode;
	private String description;
	private Integer level;
	private BigDecimal baselineForecast;
	private BigDecimal currentForecast;
	private BigDecimal variance;
	private Double variancePercent;
	private LocalDateTime baselineDate;

	// Getters and Setters
	public String getWbsCode() { return wbsCode; }
	public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public Integer getLevel() { return level; }
	public void setLevel(Integer level) { this.level = level; }

	public BigDecimal getBaselineForecast() { return baselineForecast; }
	public void setBaselineForecast(BigDecimal baselineForecast) { this.baselineForecast = baselineForecast; }

	public BigDecimal getCurrentForecast() { return currentForecast; }
	public void setCurrentForecast(BigDecimal currentForecast) { this.currentForecast = currentForecast; }

	public BigDecimal getVariance() { return variance; }
	public void setVariance(BigDecimal variance) { this.variance = variance; }

	public Double getVariancePercent() { return variancePercent; }
	public void setVariancePercent(Double variancePercent) { this.variancePercent = variancePercent; }

	public LocalDateTime getBaselineDate() { return baselineDate; }
	public void setBaselineDate(LocalDateTime baselineDate) { this.baselineDate = baselineDate; }
}
