package com.ercopac.ercopac_tracker.tasks.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "project_tasks")
public class ProjectTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

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

    @Column(name = "percent_complete")
    private Integer percentComplete;

    @Column
    private Integer priority;

    @Column(name = "schedule_mode", length = 30)
    private String scheduleMode;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "task_type", length = 30)
    private String taskType;

    @Column(name = "wbs_code", length = 50)
    private String wbsCode;

    @Column(name = "department_code", length = 30)
    private String departmentCode;

    @Column(name = "customer_milestone")
    private Boolean customerMilestone = false;

    public ProjectTask() {
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getPlannedStart() {
        return plannedStart;
    }

    public void setPlannedStart(LocalDate plannedStart) {
        this.plannedStart = plannedStart;
    }

    public LocalDate getPlannedEnd() {
        return plannedEnd;
    }

    public void setPlannedEnd(LocalDate plannedEnd) {
        this.plannedEnd = plannedEnd;
    }

    public LocalDate getBaselineStart() {
        return baselineStart;
    }

    public void setBaselineStart(LocalDate baselineStart) {
        this.baselineStart = baselineStart;
    }

    public LocalDate getBaselineEnd() {
        return baselineEnd;
    }

    public void setBaselineEnd(LocalDate baselineEnd) {
        this.baselineEnd = baselineEnd;
    }

    public Integer getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(Integer percentComplete) {
        this.percentComplete = percentComplete;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getScheduleMode() {
        return scheduleMode;
    }

    public void setScheduleMode(String scheduleMode) {
        this.scheduleMode = scheduleMode;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getWbsCode() {
        return wbsCode;
    }

    public void setWbsCode(String wbsCode) {
        this.wbsCode = wbsCode;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public Boolean getCustomerMilestone() {
        return customerMilestone;
    }

    public void setCustomerMilestone(Boolean customerMilestone) {
        this.customerMilestone = customerMilestone;
    }
}