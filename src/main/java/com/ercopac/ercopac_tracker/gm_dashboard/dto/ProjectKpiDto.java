package com.ercopac.ercopac_tracker.gm_dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectKpiDto {
    private long totalTasks;
    private long completedTasks;
    private long delayedTasks;
    private int averageTaskProgress;
    private double projectBudget;
    private long plannedDurationDays;
}