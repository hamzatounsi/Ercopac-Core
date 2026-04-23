package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;

public class FinanceOwnerMappingDto {
    private Long id;
    private String ownerKey;
    private String resourceType;
    private String roleFilter;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOwnerKey() { return ownerKey; }
    public void setOwnerKey(String ownerKey) { this.ownerKey = ownerKey; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getRoleFilter() { return roleFilter; }
    public void setRoleFilter(String roleFilter) { this.roleFilter = roleFilter; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}