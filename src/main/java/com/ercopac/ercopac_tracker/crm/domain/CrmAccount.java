package com.ercopac.ercopac_tracker.crm.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_accounts", uniqueConstraints =
        @UniqueConstraint(name = "uq_crm_account_org_name", columnNames = {"organisation_id", "name"}))
public class CrmAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;
    @Column(nullable = false, length = 180)
    private String name;
    @Column(length = 120) private String industry;
    @Column(length = 100) private String country;
    @Column(length = 100) private String city;
    @Column(length = 300) private String address;
    @Column(length = 40) private String phone;
    @Column(length = 250) private String website;
    @Column(length = 50) private String employees;
    @Column(name = "annual_revenue", precision = 18, scale = 2) private BigDecimal annualRevenue;
    @Column(length = 10) private String currency = "EUR";
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id") private AppUser owner;
    @Column(length = 4000) private String notes;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();
    @PreUpdate void updated() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation value) { organisation = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getIndustry() { return industry; }
    public void setIndustry(String value) { industry = value; }
    public String getCountry() { return country; }
    public void setCountry(String value) { country = value; }
    public String getCity() { return city; }
    public void setCity(String value) { city = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getWebsite() { return website; }
    public void setWebsite(String value) { website = value; }
    public String getEmployees() { return employees; }
    public void setEmployees(String value) { employees = value; }
    public BigDecimal getAnnualRevenue() { return annualRevenue; }
    public void setAnnualRevenue(BigDecimal value) { annualRevenue = value; }
    public String getCurrency() { return currency; }
    public void setCurrency(String value) { currency = value; }
    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser value) { owner = value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
