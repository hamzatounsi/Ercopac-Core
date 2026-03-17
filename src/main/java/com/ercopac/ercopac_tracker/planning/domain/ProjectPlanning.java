package com.ercopac.ercopac_tracker.planning.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "project_planning")
public class ProjectPlanning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long projectId;

    private LocalDate expectedStart;
    private LocalDate expectedEnd;
    private String projectCalendar;
    private Integer probability;

    @Column(length = 1000)
    private String keywords;

    @Column(length = 1000)
    private String subcontractors;

    public ProjectPlanning() {
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public LocalDate getExpectedStart() {
        return expectedStart;
    }

    public void setExpectedStart(LocalDate expectedStart) {
        this.expectedStart = expectedStart;
    }

    public LocalDate getExpectedEnd() {
        return expectedEnd;
    }

    public void setExpectedEnd(LocalDate expectedEnd) {
        this.expectedEnd = expectedEnd;
    }

    public String getProjectCalendar() {
        return projectCalendar;
    }

    public void setProjectCalendar(String projectCalendar) {
        this.projectCalendar = projectCalendar;
    }

    public Integer getProbability() {
        return probability;
    }

    public void setProbability(Integer probability) {
        this.probability = probability;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getSubcontractors() {
        return subcontractors;
    }

    public void setSubcontractors(String subcontractors) {
        this.subcontractors = subcontractors;
    }
}