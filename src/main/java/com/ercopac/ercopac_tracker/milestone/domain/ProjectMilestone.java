package com.ercopac.ercopac_tracker.milestone.domain;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_milestones")
public class ProjectMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @Column(name = "task_id")
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private ProjectTask task;

    @Column(name = "milestone_type_id", nullable = false)
    private Long milestoneTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_type_id", insertable = false, updatable = false)
    private MilestoneType milestoneType;

    @Column(name = "milestone_date", nullable = false)
    private LocalDate milestoneDate;

    @Column(length = 20)
    private String status = "A";

    @Column(name = "pm_code", length = 20)
    private String pmCode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    
    public Long getMilestoneTypeId() { return milestoneTypeId; }
    public void setMilestoneTypeId(Long milestoneTypeId) { this.milestoneTypeId = milestoneTypeId; }
    
    public LocalDate getMilestoneDate() { return milestoneDate; }
    public void setMilestoneDate(LocalDate milestoneDate) { this.milestoneDate = milestoneDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPmCode() { return pmCode; }
    public void setPmCode(String pmCode) { this.pmCode = pmCode; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public ProjectTask getTask() { return task; }
    public void setTask(ProjectTask task) { this.task = task; }
    
    public MilestoneType getMilestoneType() { return milestoneType; }
    public void setMilestoneType(MilestoneType milestoneType) { this.milestoneType = milestoneType; }
}