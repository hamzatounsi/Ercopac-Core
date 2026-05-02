package com.ercopac.ercopac_tracker.admin.dto;

public record SaveCustomerRequest(
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
        Boolean active
) {
}