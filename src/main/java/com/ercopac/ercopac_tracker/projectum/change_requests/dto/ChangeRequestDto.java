package com.ercopac.ercopac_tracker.projectum.change_requests.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChangeRequestDto {
    private Long id;
    private String code;
    private String title;
    private String status;
    private LocalDate requestDate;
    private BigDecimal valueAmount;
    private BigDecimal costAmount;
    private BigDecimal marginAmount;
    private BigDecimal marginPercent;
    private String owner;
    private String note;
    private List<ChangeRequestAttachmentDto> attachments = new ArrayList<>();
    private List<ChangeRequestHistoryDto> history = new ArrayList<>();

    public ChangeRequestDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

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

    public BigDecimal getMarginAmount() { return marginAmount; }
    public void setMarginAmount(BigDecimal marginAmount) { this.marginAmount = marginAmount; }

    public BigDecimal getMarginPercent() { return marginPercent; }
    public void setMarginPercent(BigDecimal marginPercent) { this.marginPercent = marginPercent; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<ChangeRequestAttachmentDto> getAttachments() { return attachments; }
    public void setAttachments(List<ChangeRequestAttachmentDto> attachments) { this.attachments = attachments; }

    public List<ChangeRequestHistoryDto> getHistory() { return history; }
    public void setHistory(List<ChangeRequestHistoryDto> history) { this.history = history; }
}