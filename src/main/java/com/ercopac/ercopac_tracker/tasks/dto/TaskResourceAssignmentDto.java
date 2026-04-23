package com.ercopac.ercopac_tracker.tasks.dto;

import java.math.BigDecimal;

public class TaskResourceAssignmentDto {

    private Long id;
    private Long projectId;
    private Long taskId;
    private Long assignedUserId;
    private String assignedUserName;
    private String resourceType;
    private String assignmentName;
    private Integer quantity;
    private Integer unitsPercent;
    private BigDecimal cost;

    public Long getId() { return id; }
    public TaskResourceAssignmentDto setId(Long id) { this.id = id; return this; }

    public Long getProjectId() { return projectId; }
    public TaskResourceAssignmentDto setProjectId(Long projectId) { this.projectId = projectId; return this; }

    public Long getTaskId() { return taskId; }
    public TaskResourceAssignmentDto setTaskId(Long taskId) { this.taskId = taskId; return this; }

    public Long getAssignedUserId() { return assignedUserId; }
    public TaskResourceAssignmentDto setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; return this; }

    public String getAssignedUserName() { return assignedUserName; }
    public TaskResourceAssignmentDto setAssignedUserName(String assignedUserName) { this.assignedUserName = assignedUserName; return this; }

    public String getResourceType() { return resourceType; }
    public TaskResourceAssignmentDto setResourceType(String resourceType) { this.resourceType = resourceType; return this; }

    public String getAssignmentName() { return assignmentName; }
    public TaskResourceAssignmentDto setAssignmentName(String assignmentName) { this.assignmentName = assignmentName; return this; }

    public Integer getQuantity() { return quantity; }
    public TaskResourceAssignmentDto setQuantity(Integer quantity) { this.quantity = quantity; return this; }

    public Integer getUnitsPercent() { return unitsPercent; }
    public TaskResourceAssignmentDto setUnitsPercent(Integer unitsPercent) { this.unitsPercent = unitsPercent; return this; }

    public BigDecimal getCost() { return cost; }
    public TaskResourceAssignmentDto setCost(BigDecimal cost) { this.cost = cost; return this; }
}