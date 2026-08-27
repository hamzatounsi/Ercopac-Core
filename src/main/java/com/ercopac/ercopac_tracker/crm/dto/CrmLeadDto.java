package com.ercopac.ercopac_tracker.crm.dto;
 
import java.time.LocalDateTime;
 
public class CrmLeadDto {
    private Long id;
    private String fullName;
    private String company;
    private Long accountId;
    private String accountName;
    private String jobTitle;
    private String mobile;
    private String rating;
    private String email;
    private String phone;
    private String source;
    private String status;
    private Long ownerId;
    private String ownerName;
    private boolean converted;
    private LocalDateTime convertedAt;
    private String notes;
    private boolean active;
    private LocalDateTime createdAt;
 
    public CrmLeadDto() {}
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long value) { accountId = value; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String value) { accountName = value; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String value) { jobTitle = value; }
    public String getMobile() { return mobile; }
    public void setMobile(String value) { mobile = value; }
    public String getRating() { return rating; }
    public void setRating(String value) { rating = value; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public boolean isConverted() { return converted; }
    public void setConverted(boolean converted) { this.converted = converted; }
    public LocalDateTime getConvertedAt() { return convertedAt; }
    public void setConvertedAt(LocalDateTime convertedAt) { this.convertedAt = convertedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
 
