package com.ercopac.ercopac_tracker.tasks.dto;

import java.time.LocalDate;

public class UpdateProjectTaskRequest {

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
    private String scheduleMode;

    public UpdateProjectTaskRequest() {
    }

    public String getName() {
        return name;
    }

    public UpdateProjectTaskRequest setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public UpdateProjectTaskRequest setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public UpdateProjectTaskRequest setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
        return this;
    }

    public LocalDate getBaselineStart() {
        return baselineStart;
    }

    public UpdateProjectTaskRequest setBaselineStart(LocalDate baselineStart) {
        this.baselineStart = baselineStart;
        return this;
    }

    public LocalDate getBaselineEnd() {
        return baselineEnd;
    }

    public UpdateProjectTaskRequest setBaselineEnd(LocalDate baselineEnd) {
        this.baselineEnd = baselineEnd;
        return this;
    }

    public LocalDate getPlannedStart() {
        return plannedStart;
    }

    public UpdateProjectTaskRequest setPlannedStart(LocalDate plannedStart) {
        this.plannedStart = plannedStart;
        return this;
    }

    public LocalDate getPlannedEnd() {
        return plannedEnd;
    }

    public UpdateProjectTaskRequest setPlannedEnd(LocalDate plannedEnd) {
        this.plannedEnd = plannedEnd;
        return this;
    }

    public Integer getPercentComplete() {
        return percentComplete;
    }

    public UpdateProjectTaskRequest setPercentComplete(Integer percentComplete) {
        this.percentComplete = percentComplete;
        return this;
    }

    public Integer getPriority() {
        return priority;
    }

    public UpdateProjectTaskRequest setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public String getTaskType() {
        return taskType;
    }

    public UpdateProjectTaskRequest setTaskType(String taskType) {
        this.taskType = taskType;
        return this;
    }

    public String getWbsCode() {
        return wbsCode;
    }

    public UpdateProjectTaskRequest setWbsCode(String wbsCode) {
        this.wbsCode = wbsCode;
        return this;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public UpdateProjectTaskRequest setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
        return this;
    }

    public Boolean getActive() {
        return active;
    }

    public UpdateProjectTaskRequest setActive(Boolean active) {
        this.active = active;
        return this;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public UpdateProjectTaskRequest setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
        return this;
    }

    public Boolean getCustomerMilestone() {
        return customerMilestone;
    }

    public UpdateProjectTaskRequest setCustomerMilestone(Boolean customerMilestone) {
        this.customerMilestone = customerMilestone;
        return this;
    }

    public String getScheduleMode() {
        return scheduleMode;
    }

    public UpdateProjectTaskRequest setScheduleMode(String scheduleMode) {
        this.scheduleMode = scheduleMode;
        return this;
    }
}