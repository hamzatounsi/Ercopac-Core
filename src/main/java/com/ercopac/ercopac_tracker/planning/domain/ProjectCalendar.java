package com.ercopac.ercopac_tracker.planning.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "project_calendars")
public class ProjectCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long organisationId;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 120)
    private String name;

    /**
     * Stored as comma-separated values: "1,2,3,4,5"
     * (1 = Monday, 7 = Sunday)
     */
    @Column(nullable = false, length = 50)
    private String workingDays;

    @Column(nullable = false)
    private Integer hoursPerDay;

    /**
     * Format: "08:00"
     */
    @Column(nullable = false, length = 5)
    private String startTime;

    @Column(nullable = false)
    private Boolean isDefault = false;

    // ===== Getters & Setters =====

    public Long getId() {
        return id;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(String workingDays) {
        this.workingDays = workingDays;
    }

    public Integer getHoursPerDay() {
        return hoursPerDay;
    }

    public void setHoursPerDay(Integer hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}