package com.ercopac.ercopac_tracker.crm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CrmOpportunityStageHistoryDto(Long id, Long stageId, String stageName, Integer probability,
                                             LocalDate closingDate, Long modifiedById, String modifiedByName,
                                             LocalDateTime enteredAt) {}
