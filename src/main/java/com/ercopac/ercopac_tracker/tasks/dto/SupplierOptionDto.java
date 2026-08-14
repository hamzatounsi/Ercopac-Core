package com.ercopac.ercopac_tracker.tasks.dto;

import java.util.List;

/** Active organisation supplier returned solely for task assignment selection. */
public record SupplierOptionDto(Long id, String code, String name, List<String> resourceTypeCodes) {
}
