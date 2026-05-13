package com.ercopac.ercopac_tracker.tasks.dto;
 
import java.time.LocalDate;
 
public class UpdateProjectTaskRequest {
 
    private Long parentId;          // ← NEW
    private String name;
    private String description;
    private Integer durationDays;
    private LocalDate plannedStart;
    private LocalDate plannedEnd;
    private LocalDate baselineStart;
    private LocalDate baselineEnd;
    private LocalDate actualStart;
    private LocalDate actualEnd;
    private Integer percentComplete;
    private Integer allocationPercent;
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
    private Long assignedUserId;
 
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
 
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
 
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
 
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
 
    public LocalDate getPlannedStart() { return plannedStart; }
    public void setPlannedStart(LocalDate plannedStart) { this.plannedStart = plannedStart; }
 
    public LocalDate getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(LocalDate plannedEnd) { this.plannedEnd = plannedEnd; }
 
    public LocalDate getBaselineStart() { return baselineStart; }
    public void setBaselineStart(LocalDate baselineStart) { this.baselineStart = baselineStart; }
 
    public LocalDate getBaselineEnd() { return baselineEnd; }
    public void setBaselineEnd(LocalDate baselineEnd) { this.baselineEnd = baselineEnd; }
 
    public LocalDate getActualStart() { return actualStart; }
    public void setActualStart(LocalDate actualStart) { this.actualStart = actualStart; }
 
    public LocalDate getActualEnd() { return actualEnd; }
    public void setActualEnd(LocalDate actualEnd) { this.actualEnd = actualEnd; }
 
    public Integer getPercentComplete() { return percentComplete; }
    public void setPercentComplete(Integer percentComplete) { this.percentComplete = percentComplete; }
 
    public Integer getAllocationPercent() { return allocationPercent; }
    public void setAllocationPercent(Integer allocationPercent) { this.allocationPercent = allocationPercent; }
 
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
 
    public String getScheduleMode() { return scheduleMode; }
    public void setScheduleMode(String scheduleMode) { this.scheduleMode = scheduleMode; }
 
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
 
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
 
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
 
    public String getWbsCode() { return wbsCode; }
    public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }
 
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
 
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
 
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
 
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
 
    public Integer getOutlineLevel() { return outlineLevel; }
    public void setOutlineLevel(Integer outlineLevel) { this.outlineLevel = outlineLevel; }
 
    public Boolean getCustomerMilestone() { return customerMilestone; }
    public void setCustomerMilestone(Boolean customerMilestone) { this.customerMilestone = customerMilestone; }
 
    public Long getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }
}
 