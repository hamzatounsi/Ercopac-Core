package com.ercopac.ercopac_tracker.crm.domain;

// Path: src/main/java/com/ercopac/ercopac_tracker/crm/domain/CrmLead.java

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_leads")
public class CrmLead {

    public enum Source {
        REFERRAL, TRADE_FAIR, AGENT, CUSTOMER, PARTNER, OTHER
    }

    public enum Status {
        NOT_CONTACTED, CONTACTED, CONTACT_IN_FUTURE, CONVERTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 150)
    private String company;

    @Column(length = 180)
    private String email;

    @Column(length = 40)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Source source = Source.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Status status = Status.NOT_CONTACTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @Column(nullable = false)
    private boolean converted = false;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public CrmLead() {}

    public Long getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation o) { this.organisation = o; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser owner) { this.owner = owner; }
    public boolean isConverted() { return converted; }
    public void setConverted(boolean converted) { this.converted = converted; }
    public LocalDateTime getConvertedAt() { return convertedAt; }
    public void setConvertedAt(LocalDateTime convertedAt) { this.convertedAt = convertedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}