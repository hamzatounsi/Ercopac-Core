package com.ercopac.ercopac_tracker.crm.dto;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
 
public class CrmOpportunityDto {
    private Long id;
    private String name;
    private String accountName;
    private Long stageId;
    private String stageName;
    private String stageColor;
    private BigDecimal value;
    private String currency;
    private Integer probability;
    private LocalDate closingDate;
    private Long ownerId;
    private String ownerName;
    private Long leadId;
    private boolean won;
    private boolean lost;
    private String notes;
    private LocalDateTime createdAt;
 
    public CrmOpportunityDto() {}
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public String getStageColor() { return stageColor; }
    public void setStageColor(String stageColor) { this.stageColor = stageColor; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }
    public LocalDate getClosingDate() { return closingDate; }
    public void setClosingDate(LocalDate closingDate) { this.closingDate = closingDate; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }
    public boolean isLost() { return lost; }
    public void setLost(boolean lost) { this.lost = lost; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
 