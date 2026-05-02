package com.ercopac.ercopac_tracker.admin.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;

@Entity
@Table(
    name = "project_types",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organisation_id", "code"})
    }
)
public class ProjectType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(length = 30)
    private String icon;

    @Column(length = 20)
    private String color;

    @Column(nullable = false)
    private boolean billable = false;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }

    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isBillable() { return billable; }
    public void setBillable(boolean billable) { this.billable = billable; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}