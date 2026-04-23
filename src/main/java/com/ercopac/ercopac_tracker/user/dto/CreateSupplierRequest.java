package com.ercopac.ercopac_tracker.user.dto;

import java.util.List;

public record CreateSupplierRequest(
        String name,
        String shortCode,
        String country,
        String contact,
        String website,
        List<String> departments,
        List<String> resourceTypes,
        String notes
) {}