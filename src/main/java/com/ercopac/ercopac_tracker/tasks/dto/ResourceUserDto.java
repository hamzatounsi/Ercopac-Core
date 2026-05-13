package com.ercopac.ercopac_tracker.tasks.dto;
 
public class ResourceUserDto {
    private Long id;
    private String fullName;
    private String resourceType;
    private String departmentCode;
    private String color;
 
    public ResourceUserDto() {}
 
    public ResourceUserDto(Long id, String fullName, String resourceType,
                           String departmentCode, String color) {
        this.id           = id;
        this.fullName     = fullName;
        this.resourceType = resourceType;
        this.departmentCode = departmentCode;
        this.color        = color;
    }
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
 
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
 
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
 
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
 
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}