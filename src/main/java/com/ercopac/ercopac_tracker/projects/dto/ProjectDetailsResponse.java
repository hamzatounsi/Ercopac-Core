package com.ercopac.ercopac_tracker.projects.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectDetailsResponse {

    private Long id;
    private String code;
    private String name;
    private String shortName;
    private String portfolio;
    private String orgAssignment;
    private String country;
    private String projectType;
    private String projectPhase;
    private String priority;
    private LocalDate plannedStart;
    private LocalDate plannedEnd;
    private LocalDate expectedStart;
    private LocalDate expectedEnd;
    private String projectCalendar;
    private Integer probability;
    private BigDecimal projectBudget;
    private BigDecimal totalProjectBudget;
    private Long projectManagerId;
    private String keywords;
    private String subcontractors;
    private String comment;
    private String customer;
    private String category;
    private String riskLevel;
    private BigDecimal estimatedCost;
    private String projectManagerName;
    private String programManagerName;
    private String salesManagerName;
    private Boolean archived = false;

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

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
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

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public ProjectDetailsResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public String getOrgAssignment() {
        return orgAssignment;
    }

    public void setOrgAssignment(String orgAssignment) {
        this.orgAssignment = orgAssignment;
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public LocalDate getExpectedStart() {
        return expectedStart;
    }

    public void setExpectedStart(LocalDate expectedStart) {
        this.expectedStart = expectedStart;
    }

    public LocalDate getExpectedEnd() {
        return expectedEnd;
    }

    public void setExpectedEnd(LocalDate expectedEnd) {
        this.expectedEnd = expectedEnd;
    }

    public String getProjectCalendar() {
        return projectCalendar;
    }

    public void setProjectCalendar(String projectCalendar) {
        this.projectCalendar = projectCalendar;
    }

    public Integer getProbability() {
        return probability;
    }

    public void setProbability(Integer probability) {
        this.probability = probability;
    }

    public BigDecimal getProjectBudget() {
        return projectBudget;
    }

    public void setProjectBudget(BigDecimal projectBudget) {
        this.projectBudget = projectBudget;
    }

    public BigDecimal getTotalProjectBudget() {
        return totalProjectBudget;
    }

    public void setTotalProjectBudget(BigDecimal totalProjectBudget) {
        this.totalProjectBudget = totalProjectBudget;
    }

    public Long getProjectManagerId() {
        return projectManagerId;
    }

    public void setProjectManagerId(Long projectManagerId) {
        this.projectManagerId = projectManagerId;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getSubcontractors() {
        return subcontractors;
    }

    public void setSubcontractors(String subcontractors) {
        this.subcontractors = subcontractors;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}