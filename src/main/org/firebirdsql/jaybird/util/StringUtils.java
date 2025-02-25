// SPDX-FileCopyrightText: Copyright 2019-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.jspecify.annotations.Nullable;

/**
 * Helper class for string operations
 *
 * @author Mark Rotteveel
 * @since 4
 */
public final class StringUtils {

    private StringUtils() {
        // no instances
    }

    /**
     * Trims {@code value} if non-null, returning the trimmed value, or {@code null} if {@code value} was {@code null}
     * or empty after trimming.
     *
     * @param value
     *         value to trim
     * @return Trimmed string {@code value}, or {@code null} when null, or empty after trim.
     * @see String#trim()
     */
    public static @Nullable String trimToNull(@Nullable String value) {
        if (value != null) {
            String newValue = value.trim();
            if (!newValue.isEmpty()) {
                return newValue;
            }
        }
        return null;
    }

    /**
     * Checks if {@code value} is {@code null} or empty.
     *
     * @param value
     *         value to test
     * @return {@code true} if {@code value} is {@code null} or emoty, {@code false} for non-empty strings
     * @since 6
     */
    public static boolean isNullOrEmpty(@Nullable String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Null-safe trim.
     *
     * @param stringToTrim
     *         String to trim
     * @return result of {@code stringToTrim.trim()} (or {@code null} if {@code stringToTrim} was null
     * @since 6
     */
    public static @Nullable String trim(@Nullable String stringToTrim) {
        return stringToTrim == null ? null : stringToTrim.trim();
    }
    
}
