package com.ercopac.ercopac_tracker.projectum.finance.settings.domain;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "finance_wbs_template_rows")
public class FinanceWbsTemplateRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    // ✅ NOUVEAU : Lien avec le projet (null = template global)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "resource_type", length = 100)
    private String resourceType;

    @Column(name = "level_no", nullable = false)
    private Integer level;

    @Column(name = "code_template", nullable = false, length = 100)
    private String codeTemplate;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinanceWbsRowType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @Column(name = "owner_key", length = 100)
    private String ownerKey;

    @Column(name = "hour_rate", precision = 18, scale = 2)
    private BigDecimal hourRate;

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    
    public String getCodeTemplate() { return codeTemplate; }
    public void setCodeTemplate(String codeTemplate) { this.codeTemplate = codeTemplate; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public FinanceWbsRowType getType() { return type; }
    public void setType(FinanceWbsRowType type) { this.type = type; }
    
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    
    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser owner) { this.owner = owner; }
    
    public String getOwnerKey() { return ownerKey; }
    public void setOwnerKey(String ownerKey) { this.ownerKey = ownerKey; }
    
    public BigDecimal getHourRate() { return hourRate; }
    public void setHourRate(BigDecimal hourRate) { this.hourRate = hourRate; }
}