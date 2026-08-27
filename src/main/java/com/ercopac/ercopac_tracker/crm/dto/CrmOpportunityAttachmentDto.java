package com.ercopac.ercopac_tracker.crm.dto;

import java.time.LocalDateTime;

public record CrmOpportunityAttachmentDto(Long id, String originalFileName, String contentType,
                                           long fileSize, Long uploadedById, String uploadedByName,
                                           LocalDateTime uploadedAt) {}
