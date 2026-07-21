package com.ercopac.ercopac_tracker.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignLicenceRequest(
        @NotNull(message = "User is required") Long userId,
        @NotBlank(message = "Licence type is required") String licenceType
) {
}
