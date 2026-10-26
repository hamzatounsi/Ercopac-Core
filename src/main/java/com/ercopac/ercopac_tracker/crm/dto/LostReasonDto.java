package com.ercopac.ercopac_tracker.crm.dto;

import java.time.LocalDateTime;

public record LostReasonDto(
    Long id,
    String name,
    String description,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}