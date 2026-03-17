package com.ercopac.ercopac_tracker.gm_dashboard.dto;

public class InitializedProjectResponse {

    private Long projectId;
    private String code;
    private String name;
    private String message;

    public InitializedProjectResponse() {
    }

    public InitializedProjectResponse(Long projectId, String code, String name, String message) {
        this.projectId = projectId;
        this.code = code;
        this.name = name;
        this.message = message;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}