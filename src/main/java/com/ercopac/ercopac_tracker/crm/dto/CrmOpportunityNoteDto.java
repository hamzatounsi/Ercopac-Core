package com.ercopac.ercopac_tracker.crm.dto;

import java.time.LocalDateTime;

public record CrmOpportunityNoteDto(Long id, Long authorId, String authorName, String content,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {}
