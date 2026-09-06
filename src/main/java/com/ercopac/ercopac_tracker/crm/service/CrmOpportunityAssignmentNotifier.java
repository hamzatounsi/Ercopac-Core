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
        Long organisationId = opportunity.getOrganisation().getId();
        Set<Long> previous = previousMemberIds == null ? Set.of() : previousMemberIds;
        for (AppUser member : opportunity.getTeamMembers()) {
            if (member.getId() == null || previous.contains(member.getId())
                    || member.getOrganisation() == null
                    || !Objects.equals(member.getOrganisation().getId(), organisationId)) continue;

            boolean crmEmailEnabled = preferences.findByOrganisation_IdAndUser_Id(organisationId, member.getId())
                    .map(value -> value.isEmailNotifications()).orElse(true);
            boolean userEmailEnabled = !Boolean.FALSE.equals(member.getEmailNotificationsEnabled());
            NotificationChannel channel = crmEmailEnabled && userEmailEnabled
                    ? NotificationChannel.EMAIL : NotificationChannel.APP_ALERT;
            String message = "You have been assigned to opportunity: " + opportunity.getName();
            Notification notification = notifications.create(new NotificationRequest(
                    organisationId, null, null, member.getId(), member.getEmail(), channel, "INFO",
                    "Assigned to opportunity", message,
                    "<p>" + HtmlUtils.htmlEscape(message) + "</p>"));
            notification.setLink("/crm/opportunities/" + opportunity.getId());
        }
    }
}
