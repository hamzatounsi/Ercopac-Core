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
    private LocalDate actualStart;
    private LocalDate actualEnd;
    private Integer percentComplete;
    private Integer allocationPercent;
    private Integer priority;
    private String taskType;
    private String wbsCode;
    private String departmentCode;
    private Boolean active;
    private Integer displayOrder;
    private Integer outlineLevel;
    private Boolean customerMilestone;
    private String scheduleMode;
    private String status;
    private String color;
    private Long assignedUserId;
    private String resourceType;

    public UpdateProjectTaskRequest() {
    }

    public String getName() { return name; }
    public UpdateProjectTaskRequest setName(String name) { this.name = name; return this; }

    public String getDescription() { return description; }
    public UpdateProjectTaskRequest setDescription(String description) { this.description = description; return this; }

    public Integer getDurationDays() { return durationDays; }
    public UpdateProjectTaskRequest setDurationDays(Integer durationDays) { this.durationDays = durationDays; return this; }

    public LocalDate getBaselineStart() { return baselineStart; }
    public UpdateProjectTaskRequest setBaselineStart(LocalDate baselineStart) { this.baselineStart = baselineStart; return this; }

    public LocalDate getBaselineEnd() { return baselineEnd; }
    public UpdateProjectTaskRequest setBaselineEnd(LocalDate baselineEnd) { this.baselineEnd = baselineEnd; return this; }

    public LocalDate getPlannedStart() { return plannedStart; }
    public UpdateProjectTaskRequest setPlannedStart(LocalDate plannedStart) { this.plannedStart = plannedStart; return this; }

    public LocalDate getPlannedEnd() { return plannedEnd; }
    public UpdateProjectTaskRequest setPlannedEnd(LocalDate plannedEnd) { this.plannedEnd = plannedEnd; return this; }

    public LocalDate getActualStart() { return actualStart; }
    public UpdateProjectTaskRequest setActualStart(LocalDate actualStart) { this.actualStart = actualStart; return this; }

    public LocalDate getActualEnd() { return actualEnd; }
    public UpdateProjectTaskRequest setActualEnd(LocalDate actualEnd) { this.actualEnd = actualEnd; return this; }

    public Integer getPercentComplete() { return percentComplete; }
    public UpdateProjectTaskRequest setPercentComplete(Integer percentComplete) { this.percentComplete = percentComplete; return this; }

    public Integer getAllocationPercent() { return allocationPercent; }
    public UpdateProjectTaskRequest setAllocationPercent(Integer allocationPercent) { this.allocationPercent = allocationPercent; return this; }

    public Integer getPriority() { return priority; }
    public UpdateProjectTaskRequest setPriority(Integer priority) { this.priority = priority; return this; }

    public String getTaskType() { return taskType; }
    public UpdateProjectTaskRequest setTaskType(String taskType) { this.taskType = taskType; return this; }

    public String getWbsCode() { return wbsCode; }
    public UpdateProjectTaskRequest setWbsCode(String wbsCode) { this.wbsCode = wbsCode; return this; }

    public String getDepartmentCode() { return departmentCode; }
    public UpdateProjectTaskRequest setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; return this; }

    public Boolean getActive() { return active; }
    public UpdateProjectTaskRequest setActive(Boolean active) { this.active = active; return this; }

    public Integer getDisplayOrder() { return displayOrder; }
    public UpdateProjectTaskRequest setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; return this; }

    public Integer getOutlineLevel() {
    return outlineLevel;
    }

    public UpdateProjectTaskRequest setOutlineLevel(Integer outlineLevel) {
        this.outlineLevel = outlineLevel;
        return this;
    }

    public Boolean getCustomerMilestone() { return customerMilestone; }
    public UpdateProjectTaskRequest setCustomerMilestone(Boolean customerMilestone) { this.customerMilestone = customerMilestone; return this; }

    public String getScheduleMode() { return scheduleMode; }
    public UpdateProjectTaskRequest setScheduleMode(String scheduleMode) { this.scheduleMode = scheduleMode; return this; }

    public String getStatus() { return status; }
    public UpdateProjectTaskRequest setStatus(String status) { this.status = status; return this; }

    public String getColor() { return color; }
    public UpdateProjectTaskRequest setColor(String color) { this.color = color; return this; }

    public Long getAssignedUserId() { return assignedUserId; }
    public UpdateProjectTaskRequest setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; return this; }

    public String getResourceType() { return resourceType; }
    public UpdateProjectTaskRequest setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }
}