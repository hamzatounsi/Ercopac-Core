package com.ercopac.ercopac_tracker.crm.dto;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
 
public class CrmOpportunityDto {
    private Long id;
    private String name;
    private String accountName;
    private String accountCountry;
    private Long accountId;
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
    private String contactName;
    private Long supplyCategoryId;
    private String supplyCategoryName;
    private String opportunityType;
    private String pipeline;
    private String quoteNumber;
    private LocalDate quoteRequestedDate;
    private LocalDate quoteSubmittedDate;
    private LocalDate shipmentDate;
    private String nextStep;
    private String description;
    private BigDecimal materialValue;
    private BigDecimal servicesValue;
    private BigDecimal ercopacMaterialValue;
    private BigDecimal thirdPartyMaterialValue;
    private BigDecimal ercopacResaleValue;
    private BigDecimal resaleValue;
    private boolean won;
    private boolean lost;
    private String notes;
    private LocalDateTime createdAt;
    private List<CrmUserDto> teamMembers = List.of();
 
    public CrmOpportunityDto() {}
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getAccountCountry() { return accountCountry; }
    public void setAccountCountry(String value) { accountCountry = value; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long value) { accountId = value; }
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
    public String getContactName() { return contactName; }
    public void setContactName(String value) { contactName = value; }
    public Long getSupplyCategoryId() { return supplyCategoryId; }
    public void setSupplyCategoryId(Long value) { supplyCategoryId = value; }
    public String getSupplyCategoryName() { return supplyCategoryName; }
    public void setSupplyCategoryName(String value) { supplyCategoryName = value; }
    public String getOpportunityType() { return opportunityType; }
    public void setOpportunityType(String value) { opportunityType = value; }
    public String getPipeline() { return pipeline; }
    public void setPipeline(String value) { pipeline = value; }
    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String value) { quoteNumber = value; }
    public LocalDate getQuoteRequestedDate() { return quoteRequestedDate; }
    public void setQuoteRequestedDate(LocalDate value) { quoteRequestedDate = value; }
    public LocalDate getQuoteSubmittedDate() { return quoteSubmittedDate; }
    public void setQuoteSubmittedDate(LocalDate value) { quoteSubmittedDate = value; }
    public LocalDate getShipmentDate() { return shipmentDate; }
    public void setShipmentDate(LocalDate value) { shipmentDate = value; }
    public String getNextStep() { return nextStep; }
    public void setNextStep(String value) { nextStep = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public BigDecimal getMaterialValue() { return materialValue; }
    public void setMaterialValue(BigDecimal value) { materialValue = value; }
    public BigDecimal getServicesValue() { return servicesValue; }
    public void setServicesValue(BigDecimal value) { servicesValue = value; }
    public BigDecimal getErcopacMaterialValue() { return ercopacMaterialValue; }
    public void setErcopacMaterialValue(BigDecimal value) { ercopacMaterialValue = value; }
    public BigDecimal getThirdPartyMaterialValue() { return thirdPartyMaterialValue; }
    public void setThirdPartyMaterialValue(BigDecimal value) { thirdPartyMaterialValue = value; }
    public BigDecimal getErcopacResaleValue() { return ercopacResaleValue; }
    public void setErcopacResaleValue(BigDecimal value) { ercopacResaleValue = value; }
    public BigDecimal getResaleValue() { return resaleValue; }
    public void setResaleValue(BigDecimal value) { resaleValue = value; }
    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }
    public boolean isLost() { return lost; }
    public void setLost(boolean lost) { this.lost = lost; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<CrmUserDto> getTeamMembers() { return teamMembers; }
    public void setTeamMembers(List<CrmUserDto> teamMembers) { this.teamMembers = teamMembers == null ? List.of() : teamMembers; }
}
 
