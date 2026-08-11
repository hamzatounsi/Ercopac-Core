package com.ercopac.ercopac_tracker.planning.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** JSON shape stored by the schedule template editor. */
public class ProjectTemplateTaskSnapshot {
    public Long id;
    public Long parentId;
    public String name;
    public String description;
    public Integer durationDays;
    public LocalDate plannedStart;
    public LocalDate plannedEnd;
    public LocalDate baselineStart;
    public LocalDate baselineEnd;
    public LocalDate actualStart;
    public LocalDate actualEnd;
    public Integer percentComplete;
    public Integer allocationPercent;
    public BigDecimal plannedHours;
    public BigDecimal actualHours;
    public Integer priority;
    public String scheduleMode;
    public String status;
    public String color;
    public String taskType;
    public String wbsCode;
    public String departmentCode;
    public String resourceType;
    public Boolean active;
    public Integer displayOrder;
    public Integer outlineLevel;
    public Boolean customerMilestone;
    public Long assignedUserId;
    public List<ProjectTemplateDependencySnapshot> dependencies = new ArrayList<>();

    public static class ProjectTemplateDependencySnapshot {
        public Long predecessorTaskId;
        public String dependencyType;
        public Integer lagDays;
    }
}
