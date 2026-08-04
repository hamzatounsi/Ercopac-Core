package com.ercopac.ercopac_tracker.notifications.dto;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        Long projectId,
        Long taskId,
        String channel,
        String status,
        String severity,
        String subject,
        String message,
        LocalDateTime createdAt,
        LocalDateTime sentAt,
        boolean readByUser,
        String link
) {}