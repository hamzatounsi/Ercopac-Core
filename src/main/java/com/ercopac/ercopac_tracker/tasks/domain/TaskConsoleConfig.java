package com.ercopac.ercopac_tracker.tasks.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_console_config")
public class TaskConsoleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long organisationId;
    private Long projectId;
    private Long taskId;

    private boolean checkpoint25;
    private boolean checkpoint50;
    private boolean checkpoint75;

    private String channel = "APP_ALERT";
    private boolean notifyPm = true;
    private boolean notifyOwner = true;
    private boolean notifyDeptManager = false;
    @Column(name = "notify_all", nullable = false)
    private boolean notifyEveryone = false;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }

    public Long getOrganisationId() { return organisationId; }
    public void setOrganisationId(Long organisationId) { this.organisationId = organisationId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public boolean isCheckpoint25() { return checkpoint25; }
    public void setCheckpoint25(boolean checkpoint25) { this.checkpoint25 = checkpoint25; }

    public boolean isCheckpoint50() { return checkpoint50; }
    public void setCheckpoint50(boolean checkpoint50) { this.checkpoint50 = checkpoint50; }

    public boolean isCheckpoint75() { return checkpoint75; }
    public void setCheckpoint75(boolean checkpoint75) { this.checkpoint75 = checkpoint75; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public boolean isNotifyPm() { return notifyPm; }
    public void setNotifyPm(boolean notifyPm) { this.notifyPm = notifyPm; }

    public boolean isNotifyOwner() { return notifyOwner; }
    public void setNotifyOwner(boolean notifyOwner) { this.notifyOwner = notifyOwner; }

    public boolean isNotifyDeptManager() { return notifyDeptManager; }
    public void setNotifyDeptManager(boolean notifyDeptManager) { this.notifyDeptManager = notifyDeptManager; }

    public boolean isNotifyEveryone() { return notifyEveryone; }
    public void setNotifyEveryone(boolean notifyEveryone) { this.notifyEveryone = notifyEveryone; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}