package com.ercopac.ercopac_tracker.crm.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name = "crm_notification_preferences", uniqueConstraints = @UniqueConstraint(columnNames = {"organisation_id", "user_id"}))
public class CrmNotificationPreference {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "organisation_id", nullable = false) private Organisation organisation;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private AppUser user;
    @Column(name = "email_notifications", nullable = false) private boolean emailNotifications = true;
    @Column(name = "stage_change_alerts", nullable = false) private boolean stageChangeAlerts = true;
    @Column(name = "closing_date_reminders", nullable = false) private boolean closingDateReminders = false;
    public Long getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation value) { organisation = value; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser value) { user = value; }
    public boolean isEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(boolean value) { emailNotifications = value; }
    public boolean isStageChangeAlerts() { return stageChangeAlerts; }
    public void setStageChangeAlerts(boolean value) { stageChangeAlerts = value; }
    public boolean isClosingDateReminders() { return closingDateReminders; }
    public void setClosingDateReminders(boolean value) { closingDateReminders = value; }
}
