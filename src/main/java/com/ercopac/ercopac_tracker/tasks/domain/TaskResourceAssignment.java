package com.ercopac.ercopac_tracker.tasks.domain;

import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "task_resource_assignments")
public class TaskResourceAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private AppUser assignedUser;

    @Column(name = "resource_type", length = 30)
    private String resourceType;

    @Column(name = "assignment_name", length = 150)
    private String assignmentName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "units_percent")
    private Integer unitsPercent;

    @Column(name = "cost", precision = 12, scale = 2)
    private BigDecimal cost;

    public TaskResourceAssignment() {
    }

    public Long getId() { return id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public AppUser getAssignedUser() { return assignedUser; }
    public void setAssignedUser(AppUser assignedUser) { this.assignedUser = assignedUser; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getAssignmentName() { return assignmentName; }
    public void setAssignmentName(String assignmentName) { this.assignmentName = assignmentName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getUnitsPercent() { return unitsPercent; }
    public void setUnitsPercent(Integer unitsPercent) { this.unitsPercent = unitsPercent; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
}