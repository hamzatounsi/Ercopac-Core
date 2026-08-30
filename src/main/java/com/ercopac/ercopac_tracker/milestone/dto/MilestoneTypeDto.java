package com.ercopac.ercopac_tracker.milestone.dto;

public class MilestoneTypeDto {
    private Long id;
    private Long projectId;
    private String code;
    private String label;
    private String color;
    private String letterCode;
    private boolean active;

    public MilestoneTypeDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getLetterCode() { return letterCode; }
    public void setLetterCode(String letterCode) { this.letterCode = letterCode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
