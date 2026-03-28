package com.ercopac.ercopac_tracker.gm_dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioKpiDto {
    private long totalProjects;
    private long activeProjects;
    private long delayedProjects;
    private int averageProgress;
    private double totalBudget;
    private int onTimeRate;
}