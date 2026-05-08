package com.ercopac.ercopac_tracker.crm.dto;
 
import java.time.LocalDateTime;
 
public class CrmActivityDto {
    private Long id;
    private String activityType;
    private String description;
    private Long userId;
    private String userName;
    private Long leadId;
    private Long opportunityId;
    private String metadata;
    private LocalDateTime createdAt;
 
    public CrmActivityDto() {}
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}