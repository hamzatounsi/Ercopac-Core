package com.ercopac.ercopac_tracker.tasks.dto;

import java.time.LocalDateTime;

public record TaskConsoleLogDto(
        Long id,
        Long projectId,
        Long taskId,
        String message,
        String severity,
        String channel,
        String notifyTarget,
        LocalDateTime createdAt
) {}