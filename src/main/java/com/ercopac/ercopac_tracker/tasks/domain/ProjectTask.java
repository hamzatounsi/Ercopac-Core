package com.ercopac.ercopac_tracker.tasks.domain;

import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "project_tasks")
public class ProjectTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "organisation_id", nullable = false)
    private Long organisationId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "planned_start")
    private LocalDate plannedStart;

    @Column(name = "planned_end")
    private LocalDate plannedEnd;

    @Column(name = "baseline_start")
    private LocalDate baselineStart;

    @Column(name = "baseline_end")
    private LocalDate baselineEnd;

    @Column(name = "actual_start")
    private LocalDate actualStart;

    @Column(name = "actual_end")
    private LocalDate actualEnd;

    @Column(name = "percent_complete")
    private Integer percentComplete;

    @Column(name = "allocation_percent")
    private Integer allocationPercent;

    @Column(name = "planned_hours", precision = 10, scale = 2)
    private BigDecimal plannedHours;

    @Column(name = "actual_hours", precision = 10, scale = 2)
    private BigDecimal actualHours;

    @Column
    private Integer priority;

    @Column(name = "schedule_mode", length = 30)
    private String scheduleMode;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "color", length = 20)
    private String color;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "outline_level", nullable = false)
    private Integer outlineLevel = 1;

    @Column(name = "task_type", length = 30)
    private String taskType;

    @Column(name = "wbs_code", length = 50)
    private String wbsCode;

    @Column(name = "customer_milestone")
    private Boolean customerMilestone = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private AppUser assignedUser;

    // ── PROPER FK REFERENCES (new) ──────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type_id")
    private com.ercopac.ercopac_tracker.user.ResourceType resourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private com.ercopac.ercopac_tracker.department.domain.Department department;

    // ── KEPT AS STRING for backward compatibility ───────────────
    // These are now derived from the FK relations above
    @Column(name = "resource_type", length = 30)
    private String resourceTypeCode;

    @Column(name = "department_code", length = 30)
    private String departmentCode;

    public ProjectTask() {}

    // ── Getters / Setters ────────────────────────────────────────

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getOrganisationId() { return organisationId; }
    public void setOrganisationId(Long organisationId) { this.organisationId = organisationId; }
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
    public BigDecimal getPlannedHours() { return plannedHours; }
    public void setPlannedHours(BigDecimal plannedHours) { this.plannedHours = plannedHours; }
    public BigDecimal getActualHours() { return actualHours; }
    public void setActualHours(BigDecimal actualHours) { this.actualHours = actualHours; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getScheduleMode() { return scheduleMode; }
    public void setScheduleMode(String scheduleMode) { this.scheduleMode = scheduleMode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public Integer getOutlineLevel() { return outlineLevel; }
    public void setOutlineLevel(Integer outlineLevel) { this.outlineLevel = outlineLevel; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getWbsCode() { return wbsCode; }
    public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }
    public Boolean getCustomerMilestone() { return customerMilestone; }
    public void setCustomerMilestone(Boolean customerMilestone) { this.customerMilestone = customerMilestone; }
    public AppUser getAssignedUser() { return assignedUser; }
    public void setAssignedUser(AppUser assignedUser) { this.assignedUser = assignedUser; }

    // ResourceType FK
    public com.ercopac.ercopac_tracker.user.ResourceType getResourceType() { return resourceType; }
    public void setResourceType(com.ercopac.ercopac_tracker.user.ResourceType resourceType) {
        this.resourceType = resourceType;
        // Keep string in sync
        this.resourceTypeCode = resourceType != null ? resourceType.getCode() : null;
    }

    // Department FK
    public com.ercopac.ercopac_tracker.department.domain.Department getDepartment() { return department; }
    public void setDepartment(com.ercopac.ercopac_tracker.department.domain.Department department) {
        this.department = department;
        // Keep string in sync
        this.departmentCode = department != null ? department.getCode() : null;
    }

    // String getters — derived from FK, kept for backward compat
    public String getResourceTypeCode() {
        if (resourceType != null) return resourceType.getCode();
        return resourceTypeCode;
    }
    public void setResourceTypeCode(String code) { this.resourceTypeCode = code; }

    public String getDepartmentCode() {
        if (department != null) return department.getCode();
        return departmentCode;
    }
    public void setDepartmentCode(String code) { this.departmentCode = code; }
}