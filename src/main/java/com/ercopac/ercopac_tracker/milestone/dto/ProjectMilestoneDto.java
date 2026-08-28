package com.ercopac.ercopac_tracker.milestone.dto;

import java.time.LocalDate;

public class ProjectMilestoneDto {
    private Long id;
    private Long projectId;
    private Long taskId;
    private String projectCode;
    private String projectName;
    private String taskWbsCode;
    private String taskName;
    private Long milestoneTypeId;
    private String milestoneTypeCode;
    private String milestoneTypeLabel;
    private String milestoneTypeColor;
    private String milestoneTypeLetterCode;
    private LocalDate milestoneDate;
    private String status;
    private String pmCode;
    private String notes;

    // Generate Getters and Setters for ALL fields above
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getTaskWbsCode() { return taskWbsCode; }
    public void setTaskWbsCode(String taskWbsCode) { this.taskWbsCode = taskWbsCode; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Long getMilestoneTypeId() { return milestoneTypeId; }
    public void setMilestoneTypeId(Long milestoneTypeId) { this.milestoneTypeId = milestoneTypeId; }
    public String getMilestoneTypeCode() { return milestoneTypeCode; }
    public void setMilestoneTypeCode(String milestoneTypeCode) { this.milestoneTypeCode = milestoneTypeCode; }
    public String getMilestoneTypeLabel() { return milestoneTypeLabel; }
    public void setMilestoneTypeLabel(String milestoneTypeLabel) { this.milestoneTypeLabel = milestoneTypeLabel; }
    public String getMilestoneTypeColor() { return milestoneTypeColor; }
    public void setMilestoneTypeColor(String milestoneTypeColor) { this.milestoneTypeColor = milestoneTypeColor; }
    public String getMilestoneTypeLetterCode() { return milestoneTypeLetterCode; }
    public void setMilestoneTypeLetterCode(String milestoneTypeLetterCode) { this.milestoneTypeLetterCode = milestoneTypeLetterCode; }
    public LocalDate getMilestoneDate() { return milestoneDate; }
    public void setMilestoneDate(LocalDate milestoneDate) { this.milestoneDate = milestoneDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPmCode() { return pmCode; }
    public void setPmCode(String pmCode) { this.pmCode = pmCode; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}