package com.ercopac.ercopac_tracker.employee_workspace.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateEmployeeActionStatusRequest(@NotBlank String status) {}
