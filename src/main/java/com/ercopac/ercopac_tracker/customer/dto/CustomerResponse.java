package com.ercopac.ercopac_tracker.customer.dto;

public record CustomerResponse(
        Long id,
        String customerCode,
        String name,
        String country,
        String town,
        String address,
        String vatTaxId,
        String contactPerson,
        String email,
        String phone,
        String erpId,
        boolean active,
        Long organisationId,
        String organisationName,
        CustomerReferences references
) {
}
