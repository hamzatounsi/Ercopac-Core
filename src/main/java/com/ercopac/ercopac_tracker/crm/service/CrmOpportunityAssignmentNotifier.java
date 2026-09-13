package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;
import com.ercopac.ercopac_tracker.crm.repository.CrmNotificationPreferenceRepository;
import com.ercopac.ercopac_tracker.notifications.domain.Notification;
import com.ercopac.ercopac_tracker.notifications.domain.NotificationChannel;
import com.ercopac.ercopac_tracker.notifications.dto.NotificationRequest;
import com.ercopac.ercopac_tracker.notifications.service.NotificationService;
import com.ercopac.ercopac_tracker.user.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.Objects;
import java.util.Set;

@Service
public class CrmOpportunityAssignmentNotifier {
    private final CrmNotificationPreferenceRepository preferences;
    private final NotificationService notifications;

    public CrmOpportunityAssignmentNotifier(CrmNotificationPreferenceRepository preferences,
                                             NotificationService notifications) {
        this.preferences = preferences;
        this.notifications = notifications;
    }

    @Transactional
    public void notifyNewAssignments(CrmOpportunity opportunity, Set<Long> previousMemberIds) {
        System.out.println("🔥 DEBUG [Notifier]: notifyNewAssignments appelé pour l'opportunité : " + opportunity.getName());
        
        Long organisationId = opportunity.getOrganisation().getId();
        Set<Long> previous = previousMemberIds == null ? Set.of() : previousMemberIds;
        System.out.println("🔥 DEBUG [Notifier]: Anciens IDs d'équipe : " + previous);

        for (AppUser member : opportunity.getTeamMembers()) {
            System.out.println("🔍 DEBUG [Notifier]: Vérification du membre : " + member.getFullName() + " (ID: " + member.getId() + ", Email: " + member.getEmail() + ")");
            
            if (member.getId() == null || previous.contains(member.getId())
                    || member.getOrganisation() == null
                    || !Objects.equals(member.getOrganisation().getId(), organisationId)) {
                System.out.println("⏭️ DEBUG [Notifier]: Membre IGNORÉ (déjà dans l'équipe, ID nul, ou mauvaise organisation).");
                continue;
            }

            System.out.println("✅ DEBUG [Notifier]: Membre NOUVEAU détecté ! Vérification des préférences...");
            
            boolean crmEmailEnabled = preferences.findByOrganisation_IdAndUser_Id(organisationId, member.getId())
                    .map(value -> value.isEmailNotifications()).orElse(true);
            boolean userEmailEnabled = !Boolean.FALSE.equals(member.getEmailNotificationsEnabled());
            
            System.out.println("⚙️ DEBUG [Notifier]: crmEmailEnabled=" + crmEmailEnabled + ", userEmailEnabled=" + userEmailEnabled);

            NotificationChannel channel = crmEmailEnabled && userEmailEnabled
                    ? NotificationChannel.EMAIL : NotificationChannel.APP_ALERT;
            
            String message = "You have been assigned to opportunity: " + opportunity.getName();
            System.out.println("🚀 DEBUG [Notifier]: Appel de notifications.create() pour envoyer un email à : " + member.getEmail());
            
            Notification notification = notifications.create(new NotificationRequest(
                    organisationId, null, null, member.getId(), member.getEmail(), channel, "INFO",
                    "Assigned to opportunity", message,
                    "<p>" + HtmlUtils.htmlEscape(message) + "</p>"));
            notification.setLink("/crm/opportunities/" + opportunity.getId());
            
            System.out.println("🎉 DEBUG [Notifier]: Notification créée avec succès pour " + member.getFullName());
        }
    }

    @Transactional
    public void notifyOpportunityUpdated(CrmOpportunity opportunity, AppUser actor, String subject, String message) {
        System.out.println("🔥 DEBUG [Notifier]: notifyOpportunityUpdated appelé par : " + actor.getFullName());
        Long organisationId = opportunity.getOrganisation().getId();
        for (AppUser member : opportunity.getTeamMembers()) {
            if (member.getId() == null
                    || Objects.equals(member.getId(), actor.getId())
                    || member.getOrganisation() == null
                    || !Objects.equals(member.getOrganisation().getId(), organisationId)) continue;

            boolean crmEmailEnabled = preferences.findByOrganisation_IdAndUser_Id(organisationId, member.getId())
                    .map(value -> value.isEmailNotifications()).orElse(true);
            boolean userEmailEnabled = !Boolean.FALSE.equals(member.getEmailNotificationsEnabled());
            NotificationChannel channel = crmEmailEnabled && userEmailEnabled
                    ? NotificationChannel.EMAIL : NotificationChannel.APP_ALERT;

            Notification notification = notifications.create(new NotificationRequest(
                    organisationId, null, null, member.getId(), member.getEmail(), channel, "INFO",
                    subject, message,
                    "<p>" + HtmlUtils.htmlEscape(message) + "</p>"));
            notification.setLink("/crm/opportunities/" + opportunity.getId());
        }
    }
}