package com.ercopac.ercopac_tracker.tasks.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "task_dependencies")
public class TaskDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "predecessor_task_id", nullable = false)
    private Long predecessorTaskId;

    @Column(name = "successor_task_id", nullable = false)
    private Long successorTaskId;

    @Column(name = "dependency_type", nullable = false, length = 5)
    private String dependencyType; // FS, SS, FF, SF

    @Column(name = "lag_days")
    private Integer lagDays;

    public TaskDependency() {}

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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