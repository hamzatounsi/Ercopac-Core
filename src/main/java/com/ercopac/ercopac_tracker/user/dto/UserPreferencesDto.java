package com.ercopac.ercopac_tracker.user.dto;

/**
 * UI preferences of the signed-in user. {@code language}/{@code theme} are the effective values (defaults when
 * nothing was saved); {@code languageSaved}/{@code themeSaved} tell the client whether the user chose them, so it can
 * keep a locally cached or browser choice until the first save.
 */
public record UserPreferencesDto(String language, String theme, boolean languageSaved, boolean themeSaved) {
}
