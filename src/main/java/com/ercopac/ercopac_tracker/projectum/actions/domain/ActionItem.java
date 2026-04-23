package com.ercopac.ercopac_tracker.projectum.actions.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "action_items")
public class ActionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 20)
    private String actionType; // action | issue

    @Column(length = 30)
    private String departmentCode;

    @Column(nullable = false, length = 20)
    private String priority; // high | medium | low

    @Column(nullable = false, length = 20)
    private String status; // todo | doing | review | blocked | done

    @Column(nullable = false)
    private Boolean customerVisible = false;

    private LocalDate insertedDate;
    private LocalDate dueDate;

    @OneToMany(mappedBy = "actionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionAssignee> assignees = new ArrayList<>();

    @OneToMany(mappedBy = "actionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ActionComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "actionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionAttachment> attachments = new ArrayList<>();

    public ActionItem() {}

    public Long getId() { return id; }

    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

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

    public List<ActionAssignee> getAssignees() { return assignees; }
    public void setAssignees(List<ActionAssignee> assignees) { this.assignees = assignees; }

    public List<ActionComment> getComments() { return comments; }
    public void setComments(List<ActionComment> comments) { this.comments = comments; }

    public List<ActionAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<ActionAttachment> attachments) { this.attachments = attachments; }
}