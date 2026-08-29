package com.ercopac.ercopac_tracker.crm.dto;

import java.time.LocalDateTime;

public record CrmIndustryDto(Long id, String name, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {}
