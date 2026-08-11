package com.ercopac.ercopac_tracker.planning.dto;

public class ApplyProjectTemplateResultDto {
    private Long templateId;
    private String templateName;
    private int tasksCreated;
    private int dependenciesCreated;
    private boolean alreadyApplied;

    public Long getTemplateId() { return templateId; }
    public ApplyProjectTemplateResultDto setTemplateId(Long templateId) { this.templateId = templateId; return this; }
    public String getTemplateName() { return templateName; }
    public ApplyProjectTemplateResultDto setTemplateName(String templateName) { this.templateName = templateName; return this; }
    public int getTasksCreated() { return tasksCreated; }
    public ApplyProjectTemplateResultDto setTasksCreated(int tasksCreated) { this.tasksCreated = tasksCreated; return this; }
    public int getDependenciesCreated() { return dependenciesCreated; }
    public ApplyProjectTemplateResultDto setDependenciesCreated(int dependenciesCreated) { this.dependenciesCreated = dependenciesCreated; return this; }
    public boolean isAlreadyApplied() { return alreadyApplied; }
    public ApplyProjectTemplateResultDto setAlreadyApplied(boolean alreadyApplied) { this.alreadyApplied = alreadyApplied; return this; }
}
