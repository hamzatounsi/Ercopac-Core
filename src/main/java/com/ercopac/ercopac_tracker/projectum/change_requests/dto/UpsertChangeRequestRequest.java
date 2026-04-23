package com.ercopac.ercopac_tracker.projectum.change_requests.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpsertChangeRequestRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String status;

    private LocalDate requestDate;
    private BigDecimal valueAmount;
    private BigDecimal costAmount;
    private String owner;
    private String note;

    public UpsertChangeRequestRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }

    public BigDecimal getValueAmount() { return valueAmount; }
    public void setValueAmount(BigDecimal valueAmount) { this.valueAmount = valueAmount; }

    public BigDecimal getCostAmount() { return costAmount; }
    public void setCostAmount(BigDecimal costAmount) { this.costAmount = costAmount; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}