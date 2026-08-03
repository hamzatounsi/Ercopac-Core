package com.ercopac.ercopac_tracker.tasks.domain;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import jakarta.persistence.*;

@Entity
@Table(name = "task_dependencies")
public class TaskDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, insertable = false, updatable = false)
    private Project project;

    @Column(name = "predecessor_task_id", nullable = false)
    private Long predecessorTaskId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "predecessor_task_id", nullable = false, insertable = false, updatable = false)
    private ProjectTask predecessorTask;

    @Column(name = "successor_task_id", nullable = false)
    private Long successorTaskId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "successor_task_id", nullable = false, insertable = false, updatable = false)
    private ProjectTask successorTask;

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

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Long getPredecessorTaskId() {
        return predecessorTaskId;
    }

    public void setPredecessorTaskId(Long predecessorTaskId) {
        this.predecessorTaskId = predecessorTaskId;
    }

    public ProjectTask getPredecessorTask() { return predecessorTask; }
    public void setPredecessorTask(ProjectTask predecessorTask) { this.predecessorTask = predecessorTask; }

    public Long getSuccessorTaskId() {
        return successorTaskId;
    }

    public void setSuccessorTaskId(Long successorTaskId) {
        this.successorTaskId = successorTaskId;
    }

    public ProjectTask getSuccessorTask() { return successorTask; }
    public void setSuccessorTask(ProjectTask successorTask) { this.successorTask = successorTask; }

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
