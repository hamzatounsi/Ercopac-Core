package com.ercopac.ercopac_tracker.tasks.dto;

public class TaskDependencyDto {

    private Long id;
    private Long predecessorTaskId;
    private Long successorTaskId;
    private String dependencyType;
    private Integer lagDays;

    public TaskDependencyDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPredecessorTaskId() {
        return predecessorTaskId;
    }

    public void setPredecessorTaskId(Long predecessorTaskId) {
        this.predecessorTaskId = predecessorTaskId;
    }

    public Long getSuccessorTaskId() {
        return successorTaskId;
    }

    public void setSuccessorTaskId(Long successorTaskId) {
        this.successorTaskId = successorTaskId;
    }

    public String getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(String dependencyType) {
        this.dependencyType = dependencyType;
    }

    public Integer getLagDays() {
        return lagDays;
    }

    public void setLagDays(Integer lagDays) {
        this.lagDays = lagDays;
    }
}