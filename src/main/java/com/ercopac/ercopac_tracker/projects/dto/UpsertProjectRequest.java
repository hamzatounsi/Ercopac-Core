package com.ercopac.ercopac_tracker.projects.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpsertProjectRequest {
    private String code;
    private String name;
    private String shortName;
    private String customer;
    private String category;
    private String country;
    private String projectType;
    private String projectPhase;
    private String riskLevel;
    private LocalDate plannedStart;
    private LocalDate plannedEnd;
    private BigDecimal projectBudget;
    private BigDecimal estimatedCost;
    private String projectManagerName;
    private String programManagerName;
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    public String getCustomer() {
        return customer;
    }
    public void setCustomer(String customer) {
        this.customer = customer;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getProjectType() {
        return projectType;
    }
    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }
    public String getProjectPhase() {
        return projectPhase;
    }
    public void setProjectPhase(String projectPhase) {
        this.projectPhase = projectPhase;
    }
    public String getRiskLevel() {
        return riskLevel;
    }
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    public LocalDate getPlannedStart() {
        return plannedStart;
    }
    public void setPlannedStart(LocalDate plannedStart) {
        this.plannedStart = plannedStart;
    }
    public LocalDate getPlannedEnd() {
        return plannedEnd;
    }
    public void setPlannedEnd(LocalDate plannedEnd) {
        this.plannedEnd = plannedEnd;
    }
    public BigDecimal getProjectBudget() {
        return projectBudget;
    }
    public void setProjectBudget(BigDecimal projectBudget) {
        this.projectBudget = projectBudget;
    }
    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }
    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
    public String getProjectManagerName() {
        return projectManagerName;
    }
    public void setProjectManagerName(String projectManagerName) {
        this.projectManagerName = projectManagerName;
    }
    public String getProgramManagerName() {
        return programManagerName;
    }
    public void setProgramManagerName(String programManagerName) {
        this.programManagerName = programManagerName;
    }
    public String getSalesManagerName() {
        return salesManagerName;
    }
    public void setSalesManagerName(String salesManagerName) {
        this.salesManagerName = salesManagerName;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    private String salesManagerName;
    private String comment;

    // getters and setters
}