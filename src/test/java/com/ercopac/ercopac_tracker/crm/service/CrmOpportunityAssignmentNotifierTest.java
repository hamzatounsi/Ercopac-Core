package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.domain.CrmNotificationPreference;
import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;
import com.ercopac.ercopac_tracker.crm.repository.CrmNotificationPreferenceRepository;
import com.ercopac.ercopac_tracker.notifications.domain.Notification;
import com.ercopac.ercopac_tracker.notifications.domain.NotificationChannel;
import com.ercopac.ercopac_tracker.notifications.dto.NotificationRequest;
import com.ercopac.ercopac_tracker.notifications.service.NotificationService;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrmOpportunityAssignmentNotifierTest {
    @Mock CrmNotificationPreferenceRepository preferences;
    @Mock NotificationService notifications;
    @Mock Organisation organisation;
    @Mock AppUser userA;
    @Mock AppUser userB;
    CrmOpportunityAssignmentNotifier notifier;
    CrmOpportunity opportunity;

    @BeforeEach
    void setUp() {
        notifier = new CrmOpportunityAssignmentNotifier(preferences, notifications);
        lenient().when(organisation.getId()).thenReturn(11L);
        configureUser(userA, 1L, "a@example.com");
        configureUser(userB, 2L, "b@example.com");
        lenient().when(notifications.create(any())).thenAnswer(invocation -> new Notification());
        opportunity = new CrmOpportunity();
        opportunity.setOrganisation(organisation);
        opportunity.setName("Tunisia opportunity");
    }

    @Test
    void newlyAddedUserGetsExactlyOneNotification() {
        opportunity.getTeamMembers().add(userA);

        notifier.notifyNewAssignments(opportunity, Set.of());

        ArgumentCaptor<NotificationRequest> request = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notifications).create(request.capture());
        assertEquals(1L, request.getValue().recipientUserId());
        assertEquals("Assigned to opportunity", request.getValue().subject());
    }

    @Test
    void unchangedTeamCreatesNoNotification() {
        opportunity.getTeamMembers().add(userA);

        notifier.notifyNewAssignments(opportunity, Set.of(1L));

        verifyNoInteractions(notifications);
    }

    @Test
    void addingSecondUserOnlyNotifiesSecondUser() {
        opportunity.getTeamMembers().add(userA);
        opportunity.getTeamMembers().add(userB);

        notifier.notifyNewAssignments(opportunity, Set.of(1L));

        ArgumentCaptor<NotificationRequest> request = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notifications).create(request.capture());
        assertEquals(2L, request.getValue().recipientUserId());
    }

    @Test
    void creationWithTwoMembersNotifiesBothAndReusesEmailPreference() {
        CrmNotificationPreference emailDisabled = new CrmNotificationPreference();
        emailDisabled.setEmailNotifications(false);
        when(preferences.findByOrganisation_IdAndUser_Id(11L, 1L)).thenReturn(Optional.empty());
        when(preferences.findByOrganisation_IdAndUser_Id(11L, 2L)).thenReturn(Optional.of(emailDisabled));
        opportunity.getTeamMembers().add(userA);
        opportunity.getTeamMembers().add(userB);

        notifier.notifyNewAssignments(opportunity, Set.of());

        ArgumentCaptor<NotificationRequest> requests = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notifications, times(2)).create(requests.capture());
        assertEquals(NotificationChannel.EMAIL, requests.getAllValues().get(0).channel());
        assertEquals(NotificationChannel.APP_ALERT, requests.getAllValues().get(1).channel());
    }

    private void configureUser(AppUser user, Long id, String email) {
        lenient().when(user.getId()).thenReturn(id);
        lenient().when(user.getEmail()).thenReturn(email);
        lenient().when(user.getOrganisation()).thenReturn(organisation);
        lenient().when(user.getEmailNotificationsEnabled()).thenReturn(true);
    }
}
