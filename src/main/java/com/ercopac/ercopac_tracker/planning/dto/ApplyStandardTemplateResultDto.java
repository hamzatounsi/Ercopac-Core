package com.ercopac.ercopac_tracker.planning.dto;

public class ApplyStandardTemplateResultDto {

    private Long projectId;
    private String templateName;
    private int tasksCreated;
    private int dependenciesCreated;

    public Long getProjectId() {
        return projectId;
    }

    public ApplyStandardTemplateResultDto setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getTemplateName() {
        return templateName;
    }

    public ApplyStandardTemplateResultDto setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public int getTasksCreated() {
        return tasksCreated;
    }

    public ApplyStandardTemplateResultDto setTasksCreated(int tasksCreated) {
        this.tasksCreated = tasksCreated;
        return this;
    }

    public int getDependenciesCreated() {
        return dependenciesCreated;
    }

    public ApplyStandardTemplateResultDto setDependenciesCreated(int dependenciesCreated) {
        this.dependenciesCreated = dependenciesCreated;
        return this;
    }
}