package com.ercopac.ercopac_tracker.common;

import java.util.Locale;

/**
 * Single place that defines how business names (resources/users, customers,
 * organisations, suppliers) are stored: trimmed, inner whitespace collapsed,
 * upper-cased with {@link Locale#ROOT} so the result does not depend on the
 * server locale (e.g. the Turkish dotless i).
 *
 * Only names go through here; emails, logins, codes and free text do not.
 */
public final class NameNormalizer {

    private NameNormalizer() {
    }

    /** Uppercased, trimmed name, or {@code null} when the value is blank. */
    public static String upper(String value) {
        String key = collapse(value);
        return key == null ? null : key.toUpperCase(Locale.ROOT);
    }

    /**
     * Comparison key for case-insensitive matching (uniqueness checks,
     * legacy free-text matching): trimmed, whitespace collapsed, uppercased.
     */
    public static String matchKey(String value) {
        return upper(value);
    }

    /** Trimmed, inner whitespace collapsed, case kept; {@code null} when blank. */
    public static String collapse(String value) {
        if (value == null) {
            return null;
        }
        String collapsed = value.trim().replaceAll("\\s+", " ");
        return collapsed.isEmpty() ? null : collapsed;
    }
}
