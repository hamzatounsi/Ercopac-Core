package com.ercopac.ercopac_tracker.tasks.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class CreateTaskRequest {

    @NotBlank
    private String name;

    private String description;

    @Min(1)
    private Integer durationDays;

    private LocalDate plannedStart;

    private LocalDate plannedEnd;

    @Min(0)
    @Max(100)
    private Integer percentComplete;

    @Min(0)
    @Max(1000)
    private Integer priority;

    private String scheduleMode;

    private Boolean active;

    private Integer displayOrder;

    public CreateTaskRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public LocalDate getPlannedStart() {
        return plannedStart;
    }

    public void setPlannedStart(LocalDate plannedStart) {
        this.plannedStart = plannedStart;
    }

    public LocalDate getPlannedEnd() {
        return plannedEnd;
    }

    public void setPlannedEnd(LocalDate plannedEnd) {
        this.plannedEnd = plannedEnd;
    }

    public Integer getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(Integer percentComplete) {
        this.percentComplete = percentComplete;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getScheduleMode() {
        return scheduleMode;
    }

    public void setScheduleMode(String scheduleMode) {
        this.scheduleMode = scheduleMode;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}