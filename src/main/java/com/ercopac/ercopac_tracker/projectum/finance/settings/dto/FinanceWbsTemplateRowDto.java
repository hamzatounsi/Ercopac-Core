package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;

import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceWbsRowType;
import java.math.BigDecimal;

public class FinanceWbsTemplateRowDto {
    private Long id;
    private Integer sortOrder;
    private Integer level;
    private String codeTemplate;
    private String description;
    private FinanceWbsRowType type;
    
    // ✅ NEW: Department fields
    private Long departmentId;
    private String departmentName;
    
    // ✅ NEW: Owner fields
    private Long ownerId;
    private String ownerName;
    
    private String ownerKey;
    private BigDecimal hourRate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    
    public String getCodeTemplate() { return codeTemplate; }
    public void setCodeTemplate(String codeTemplate) { this.codeTemplate = codeTemplate; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public FinanceWbsRowType getType() { return type; }
    public void setType(FinanceWbsRowType type) { this.type = type; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public String getOwnerKey() { return ownerKey; }
    public void setOwnerKey(String ownerKey) { this.ownerKey = ownerKey; }
    
    public BigDecimal getHourRate() { return hourRate; }
    public void setHourRate(BigDecimal hourRate) { this.hourRate = hourRate; }
}