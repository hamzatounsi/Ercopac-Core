package com.ercopac.ercopac_tracker.user;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "resource_type_dept_map", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"resource_type_id", "department_id"})
})
public class ResourceTypeDeptMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type_id", nullable = false)
    private ResourceType resourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(length = 20)
    private String colour;

    @Column(name = "default_rate", precision = 12, scale = 2)
    private BigDecimal defaultRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    public ResourceTypeDeptMap() {}

    public Long getId() { return id; }
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }
    public BigDecimal getDefaultRate() { return defaultRate; }
    public void setDefaultRate(BigDecimal defaultRate) { this.defaultRate = defaultRate; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }
}