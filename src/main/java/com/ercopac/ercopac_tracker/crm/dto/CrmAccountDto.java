package com.ercopac.ercopac_tracker.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CrmAccountDto(
        Long id, String name, String industry, Long industryId, String country, String city,
        String address, String phone, String website, String employees,
        BigDecimal annualRevenue, String currency, Long ownerId, String ownerName,
        String notes, long leadCount, long opportunityCount, BigDecimal pipelineValue,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}
