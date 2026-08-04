package com.ercopac.ercopac_tracker.gm_dashboard.dto;

public class PortfolioKpiDto {
    private long totalProjects;
    private long activeProjects;
    private long delayedProjects;
    private int averageProgress;
    private double totalBudget;
    private int onTimeRate;

    public PortfolioKpiDto() {}

    public PortfolioKpiDto(long totalProjects, long activeProjects, long delayedProjects,
                           int averageProgress, double totalBudget, int onTimeRate) {
        this.totalProjects = totalProjects;
        this.activeProjects = activeProjects;
        this.delayedProjects = delayedProjects;
        this.averageProgress = averageProgress;
        this.totalBudget = totalBudget;
        this.onTimeRate = onTimeRate;
    }

    public long getTotalProjects() { return totalProjects; }
    public void setTotalProjects(long totalProjects) { this.totalProjects = totalProjects; }
    public long getActiveProjects() { return activeProjects; }
    public void setActiveProjects(long activeProjects) { this.activeProjects = activeProjects; }
    public long getDelayedProjects() { return delayedProjects; }
    public void setDelayedProjects(long delayedProjects) { this.delayedProjects = delayedProjects; }
    public int getAverageProgress() { return averageProgress; }
    public void setAverageProgress(int averageProgress) { this.averageProgress = averageProgress; }
    public double getTotalBudget() { return totalBudget; }
    public void setTotalBudget(double totalBudget) { this.totalBudget = totalBudget; }
    public int getOnTimeRate() { return onTimeRate; }
    public void setOnTimeRate(int onTimeRate) { this.onTimeRate = onTimeRate; }
}
