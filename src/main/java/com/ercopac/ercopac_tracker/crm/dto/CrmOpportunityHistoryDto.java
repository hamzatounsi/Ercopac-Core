package com.ercopac.ercopac_tracker.crm.dto;

import java.time.LocalDateTime;

public record CrmOpportunityHistoryDto(Long id, String fieldName, String oldValue, String newValue,
                                        Long changedById, String changedByName, LocalDateTime createdAt) {}
