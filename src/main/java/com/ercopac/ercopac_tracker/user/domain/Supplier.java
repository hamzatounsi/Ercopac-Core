package com.ercopac.ercopac_tracker.user.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String shortCode;

    @Column(length = 80)
    private String country;

    @Column(length = 150)
    private String contact;

    @Column(length = 255)
    private String website;

    @Column(name = "departments_csv", length = 1000)
    private String departmentsCsv;

    @Column(name = "resource_types_csv", length = 1000)
    private String resourceTypesCsv;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDepartmentsCsv() {
        return departmentsCsv;
    }

    public void setDepartmentsCsv(String departmentsCsv) {
        this.departmentsCsv = departmentsCsv;
    }

    public String getResourceTypesCsv() {
        return resourceTypesCsv;
    }

    public void setResourceTypesCsv(String resourceTypesCsv) {
        this.resourceTypesCsv = resourceTypesCsv;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}