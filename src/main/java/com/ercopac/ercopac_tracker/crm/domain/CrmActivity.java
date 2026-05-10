package com.ercopac.ercopac_tracker.crm.domain;

// Path: src/main/java/com/ercopac/ercopac_tracker/crm/domain/CrmActivity.java

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_activities")
public class CrmActivity {

    public enum ActivityType {
        EMAIL_SENT, STAGE_UPDATED, OFFER_ATTACHED,
        LEAD_CREATED, OPPORTUNITY_CREATED, NOTE_ADDED,
        LEAD_CONVERTED, DEAL_WON, DEAL_LOST
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private CrmLead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id")
    private CrmOpportunity opportunity;

    @Column(length = 500)
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CrmActivity() {}

    // Factory method for quick creation
    public static CrmActivity of(Organisation org, AppUser user,
                                  ActivityType type, String description) {
        CrmActivity a = new CrmActivity();
        a.organisation = org;
        a.user = user;
        a.activityType = type;
        a.description = description;
        return a;
    }

    public Long getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation o) { this.organisation = o; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public CrmLead getLead() { return lead; }
    public void setLead(CrmLead lead) { this.lead = lead; }
    public CrmOpportunity getOpportunity() { return opportunity; }
    public void setOpportunity(CrmOpportunity opportunity) { this.opportunity = opportunity; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}