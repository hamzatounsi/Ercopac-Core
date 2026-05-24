package com.ercopac.ercopac_tracker.projectum.risks.dto;

public class RiskExposureItemDto {
    private Long riskId;
    private String riskCode;
    private String description;
    private int riskValue;
    private String riskLevel;

    public RiskExposureItemDto() {}

    public RiskExposureItemDto(Long riskId, String riskCode, 
                                String description, int riskValue, String riskLevel) {
        this.riskId = riskId;
        this.riskCode = riskCode;
        this.description = description;
        this.riskValue = riskValue;
        this.riskLevel = riskLevel;
    }

    public Long getRiskId() { return riskId; }
    public void setRiskId(Long riskId) { this.riskId = riskId; }

    public String getRiskCode() { return riskCode; }
    public void setRiskCode(String riskCode) { this.riskCode = riskCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getRiskValue() { return riskValue; }
    public void setRiskValue(int riskValue) { this.riskValue = riskValue; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}