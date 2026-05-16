package com.ercopac.ercopac_tracker.tasks.dto;

import java.time.LocalDateTime;

public record TaskConsoleConfigDto(
        Long id,
        Long projectId,
        Long taskId,
        boolean checkpoint25,
        boolean checkpoint50,
        boolean checkpoint75,
        String channel,
        boolean notifyPm,
        boolean notifyOwner,
        boolean notifyDeptManager,
        boolean notifyEveryone,
        LocalDateTime updatedAt
) {}