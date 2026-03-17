package com.ercopac.ercopac_tracker.tasks.dto;

import java.time.LocalDate;

public class ProjectScheduleTaskResponse {

    private Long id;
    private Long projectId;
    private String name;
    private String description;
    private Integer durationDays;
    private LocalDate baselineStart;
    private LocalDate baselineEnd;
    private LocalDate plannedStart;
    private LocalDate plannedEnd;
    private Integer percentComplete;
    private Integer priority;
    private String taskType;
    private String wbsCode;
    private String departmentCode;
    private Boolean active;
    private Integer displayOrder;
    private Boolean customerMilestone;

    public ProjectScheduleTaskResponse() {
    }

    public Long getId() {
        return id;
    }

    public ProjectScheduleTaskResponse setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getProjectId() {
        return projectId;
    }

    public ProjectScheduleTaskResponse setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getName() {
        return name;
    }

    public ProjectScheduleTaskResponse setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ProjectScheduleTaskResponse setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public ProjectScheduleTaskResponse setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
        return this;
    }

    public LocalDate getBaselineStart() {
        return baselineStart;
    }

    public ProjectScheduleTaskResponse setBaselineStart(LocalDate baselineStart) {
        this.baselineStart = baselineStart;
        return this;
    }

    public LocalDate getBaselineEnd() {
        return baselineEnd;
    }

    public ProjectScheduleTaskResponse setBaselineEnd(LocalDate baselineEnd) {
        this.baselineEnd = baselineEnd;
        return this;
    }

    public LocalDate getPlannedStart() {
        return plannedStart;
    }

    public ProjectScheduleTaskResponse setPlannedStart(LocalDate plannedStart) {
        this.plannedStart = plannedStart;
        return this;
    }

    public LocalDate getPlannedEnd() {
        return plannedEnd;
    }

    public ProjectScheduleTaskResponse setPlannedEnd(LocalDate plannedEnd) {
        this.plannedEnd = plannedEnd;
        return this;
    }

    public Integer getPercentComplete() {
        return percentComplete;
    }

    public ProjectScheduleTaskResponse setPercentComplete(Integer percentComplete) {
        this.percentComplete = percentComplete;
        return this;
    }

    public Integer getPriority() {
        return priority;
    }

    public ProjectScheduleTaskResponse setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public String getTaskType() {
        return taskType;
    }

    public ProjectScheduleTaskResponse setTaskType(String taskType) {
        this.taskType = taskType;
        return this;
    }

    public String getWbsCode() {
        return wbsCode;
    }

    public ProjectScheduleTaskResponse setWbsCode(String wbsCode) {
        this.wbsCode = wbsCode;
        return this;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public ProjectScheduleTaskResponse setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
        return this;
    }

    public Boolean getActive() {
        return active;
    }

    public ProjectScheduleTaskResponse setActive(Boolean active) {
        this.active = active;
        return this;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public ProjectScheduleTaskResponse setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
        return this;
    }

    public Boolean getCustomerMilestone() {
        return customerMilestone;
    }

    public ProjectScheduleTaskResponse setCustomerMilestone(Boolean customerMilestone) {
        this.customerMilestone = customerMilestone;
        return this;
    }
}