package com.ercopac.ercopac_tracker.notifications.dto;

import com.ercopac.ercopac_tracker.notifications.domain.NotificationChannel;

public record NotificationRequest(
        Long organisationId,
        Long projectId,
        Long taskId,
        Long recipientUserId,
        String recipientEmail,
        NotificationChannel channel,
        String severity,
        String subject,
        String message,
        String htmlBody
) {}