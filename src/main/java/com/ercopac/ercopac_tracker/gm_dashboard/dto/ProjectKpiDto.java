package com.ercopac.ercopac_tracker.gm_dashboard.dto;

public class ProjectKpiDto {
    private long totalTasks;
    private long completedTasks;
    private long delayedTasks;
    private int averageTaskProgress;
    private double projectBudget;
    private long plannedDurationDays;

    public ProjectKpiDto() {}

    public ProjectKpiDto(long totalTasks, long completedTasks, long delayedTasks,
                         int averageTaskProgress, double projectBudget, long plannedDurationDays) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.delayedTasks = delayedTasks;
        this.averageTaskProgress = averageTaskProgress;
        this.projectBudget = projectBudget;
        this.plannedDurationDays = plannedDurationDays;
    }

    public long getTotalTasks() { return totalTasks; }
    public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
    public long getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }
    public long getDelayedTasks() { return delayedTasks; }
    public void setDelayedTasks(long delayedTasks) { this.delayedTasks = delayedTasks; }
    public int getAverageTaskProgress() { return averageTaskProgress; }
    public void setAverageTaskProgress(int averageTaskProgress) { this.averageTaskProgress = averageTaskProgress; }
    public double getProjectBudget() { return projectBudget; }
    public void setProjectBudget(double projectBudget) { this.projectBudget = projectBudget; }
    public long getPlannedDurationDays() { return plannedDurationDays; }
    public void setPlannedDurationDays(long plannedDurationDays) { this.plannedDurationDays = plannedDurationDays; }
}
