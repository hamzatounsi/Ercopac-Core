package com.ercopac.ercopac_tracker.projectum.actions.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActionItemDto {
    private Long id;
    private String title;
    private String description;
    private String actionType;
    private String departmentCode;
    private String priority;
    private String status;
    private Boolean customerVisible;
    private LocalDate insertedDate;
    private LocalDate dueDate;
    private List<String> assignees = new ArrayList<>();
    private List<ActionCommentDto> comments = new ArrayList<>();
    private List<ActionAttachmentDto> attachments = new ArrayList<>();

    public ActionItemDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getCustomerVisible() { return customerVisible; }
    public void setCustomerVisible(Boolean customerVisible) { this.customerVisible = customerVisible; }

    public LocalDate getInsertedDate() { return insertedDate; }
    public void setInsertedDate(LocalDate insertedDate) { this.insertedDate = insertedDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public List<String> getAssignees() { return assignees; }
    public void setAssignees(List<String> assignees) { this.assignees = assignees; }

    public List<ActionCommentDto> getComments() { return comments; }
    public void setComments(List<ActionCommentDto> comments) { this.comments = comments; }

    public List<ActionAttachmentDto> getAttachments() { return attachments; }
    public void setAttachments(List<ActionAttachmentDto> attachments) { this.attachments = attachments; }
}