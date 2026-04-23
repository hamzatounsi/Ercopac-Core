package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;

public class ApplyFinanceTemplateResultDto {
    private int projectsProcessed;
    private int rowsGenerated;

    public int getProjectsProcessed() {
        return projectsProcessed;
    }

    public void setProjectsProcessed(int projectsProcessed) {
        this.projectsProcessed = projectsProcessed;
    }

    public int getRowsGenerated() {
        return rowsGenerated;
    }

    public void setRowsGenerated(int rowsGenerated) {
        this.rowsGenerated = rowsGenerated;
    }
}