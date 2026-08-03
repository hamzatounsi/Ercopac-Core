package com.ercopac.ercopac_tracker.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveCustomerRequest(
        @NotBlank(message = "Customer code is required")
        @Pattern(regexp = "[A-Za-z0-9_-]{2,40}", message = "Customer code must contain 2 to 40 letters, numbers, hyphens or underscores") String customerCode,
        @NotBlank(message = "Customer name is required")
        @Size(max = 150, message = "Customer name must not exceed 150 characters") String name,
        @Size(max = 80, message = "Country must not exceed 80 characters") String country,
        @Size(max = 100, message = "Town must not exceed 100 characters") String town,
        @Size(max = 250, message = "Address must not exceed 250 characters") String address,
        @Size(max = 80, message = "VAT or tax ID must not exceed 80 characters") String vatTaxId,
        @Size(max = 150, message = "Contact person must not exceed 150 characters") String contactPerson,
        @Email(message = "Enter a valid customer email")
        @Size(max = 180, message = "Email must not exceed 180 characters") String email,
        @Size(max = 50, message = "Phone must not exceed 50 characters") String phone,
        @Size(max = 80, message = "ERP ID must not exceed 80 characters") String erpId,
        Boolean active
) {
}
