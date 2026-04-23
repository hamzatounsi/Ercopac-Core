package com.ercopac.ercopac_tracker.tasks.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private LocalDate actualStart;
    private LocalDate actualEnd;

    private Integer percentComplete;
    private Integer allocationPercent;
    private BigDecimal plannedHours;
    private BigDecimal actualHours;

    private Integer priority;
    private String scheduleMode;
    private String status;
    private String color;
    private String taskType;
    private String wbsCode;
    private String departmentCode;
    private String resourceType;

    private Boolean active;
    private Integer displayOrder;
    private Integer outlineLevel;
    private Boolean customerMilestone;

    private String predecessorLabel;
    private List<TaskDependencyDto> dependencies;

    private Long assignedUserId;
    private String assignedUserName;

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

    public LocalDate getActualStart() {
        return actualStart;
    }

    public ProjectScheduleTaskResponse setActualStart(LocalDate actualStart) {
        this.actualStart = actualStart;
        return this;
    }

    public LocalDate getActualEnd() {
        return actualEnd;
    }

    public ProjectScheduleTaskResponse setActualEnd(LocalDate actualEnd) {
        this.actualEnd = actualEnd;
        return this;
    }

    public Integer getPercentComplete() {
        return percentComplete;
    }

    public ProjectScheduleTaskResponse setPercentComplete(Integer percentComplete) {
        this.percentComplete = percentComplete;
        return this;
    }

    public Integer getAllocationPercent() {
        return allocationPercent;
    }

    public ProjectScheduleTaskResponse setAllocationPercent(Integer allocationPercent) {
        this.allocationPercent = allocationPercent;
        return this;
    }

    public BigDecimal getPlannedHours() {
        return plannedHours;
    }

    public ProjectScheduleTaskResponse setPlannedHours(BigDecimal plannedHours) {
        this.plannedHours = plannedHours;
        return this;
    }

    public BigDecimal getActualHours() {
        return actualHours;
    }

    public ProjectScheduleTaskResponse setActualHours(BigDecimal actualHours) {
        this.actualHours = actualHours;
        return this;
    }

    public Integer getPriority() {
        return priority;
    }

    public ProjectScheduleTaskResponse setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public String getScheduleMode() {
        return scheduleMode;
    }

    public ProjectScheduleTaskResponse setScheduleMode(String scheduleMode) {
        this.scheduleMode = scheduleMode;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ProjectScheduleTaskResponse setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getColor() {
        return color;
    }

    public ProjectScheduleTaskResponse setColor(String color) {
        this.color = color;
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

    public String getResourceType() {
        return resourceType;
    }

    public ProjectScheduleTaskResponse setResourceType(String resourceType) {
        this.resourceType = resourceType;
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

    public Integer getOutlineLevel() {
    return outlineLevel;
}

    public ProjectScheduleTaskResponse setOutlineLevel(Integer outlineLevel) {
        this.outlineLevel = outlineLevel;
        return this;
    }
    public Boolean getCustomerMilestone() {
        return customerMilestone;
    }

    public ProjectScheduleTaskResponse setCustomerMilestone(Boolean customerMilestone) {
        this.customerMilestone = customerMilestone;
        return this;
    }

    public String getPredecessorLabel() {
        return predecessorLabel;
    }

    public ProjectScheduleTaskResponse setPredecessorLabel(String predecessorLabel) {
        this.predecessorLabel = predecessorLabel;
        return this;
    }

    public List<TaskDependencyDto> getDependencies() {
        return dependencies;
    }

    public ProjectScheduleTaskResponse setDependencies(List<TaskDependencyDto> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public ProjectScheduleTaskResponse setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
        return this;
    }

    public String getAssignedUserName() {
        return assignedUserName;
    }

    public ProjectScheduleTaskResponse setAssignedUserName(String assignedUserName) {
        this.assignedUserName = assignedUserName;
        return this;
    }
}