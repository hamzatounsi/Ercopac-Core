package com.ercopac.ercopac_tracker.company_dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record CompanyDashboardDto(
        String organisationName,
        int totalProjects, int activeProjects, int onScheduleProjects, int atRiskProjects, int delayedProjects,
        int averageProgress, BigDecimal totalBudget, BigDecimal actualCost, BigDecimal forecastCost,
        int openRisks, int highRisks, int openActions, int overdueActions,
        List<ProjectRow> projects, List<RiskRow> topRisks, List<AlertRow> alerts
) {
    public record ProjectRow(String name, String code, String manager, String phase, int progress, String health, BigDecimal budget) {}
    public record RiskRow(String description, String project, String impact, Integer probability, String state) {}
    public record AlertRow(String severity, String title, String detail) {}
}
