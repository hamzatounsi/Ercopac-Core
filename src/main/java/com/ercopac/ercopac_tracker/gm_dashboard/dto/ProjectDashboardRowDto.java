package com.ercopac.ercopac_tracker.gm_dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectDashboardRowDto {

    private Long id;
    private String code;
    private String name;
    private String shortName;

    private String customer;
    private String category;

    private String country;
    private String portfolio;

    private String projectType;
    private String projectPhase;
    private String riskLevel;

    private String projectManagerName;
    private String programManagerName;
    private String salesManagerName;

    private LocalDate plannedStart;
    private LocalDate plannedEnd;

    private BigDecimal projectBudget;
    private BigDecimal estimatedCost;

    private Integer progressPercent;

    private Boolean archived;
    private String timeHealth;

    public ProjectDashboardRowDto() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPortfolio() { return portfolio; }
    public void setPortfolio(String portfolio) { this.portfolio = portfolio; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public String getProjectPhase() { return projectPhase; }
    public void setProjectPhase(String projectPhase) { this.projectPhase = projectPhase; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getProjectManagerName() { return projectManagerName; }
    public void setProjectManagerName(String projectManagerName) { this.projectManagerName = projectManagerName; }

    public String getProgramManagerName() { return programManagerName; }
    public void setProgramManagerName(String programManagerName) { this.programManagerName = programManagerName; }

    public String getSalesManagerName() { return salesManagerName; }
    public void setSalesManagerName(String salesManagerName) { this.salesManagerName = salesManagerName; }

    public LocalDate getPlannedStart() { return plannedStart; }
    public void setPlannedStart(LocalDate plannedStart) { this.plannedStart = plannedStart; }

    public LocalDate getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(LocalDate plannedEnd) { this.plannedEnd = plannedEnd; }

    public BigDecimal getProjectBudget() { return projectBudget; }
    public void setProjectBudget(BigDecimal projectBudget) { this.projectBudget = projectBudget; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }

    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }

    public String getTimeHealth() { return timeHealth; }
    public void setTimeHealth(String timeHealth) { this.timeHealth = timeHealth; }
}