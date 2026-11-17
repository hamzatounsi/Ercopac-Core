package com.ercopac.ercopac_tracker.user.dto;

import jakarta.validation.constraints.Pattern;

/** Partial update: a null field is left unchanged. */
public record UpdateUserPreferencesRequest(
        @Pattern(regexp = "en|fr|de|ar|it|es|pt|tr|nl|zh", message = "must be one of en, fr, de, ar, it, es, pt, tr, nl, zh")
        String language,
        @Pattern(regexp = "light|dim|dark", message = "must be one of light, dim, dark")
        String theme
) {
}
