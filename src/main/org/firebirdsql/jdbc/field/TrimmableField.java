// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Trim behaviour of {@code getString} in string fields.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
@NullMarked
public interface TrimmableField {

    /**
     * Enable or disable trimming of trailing spaces.
     * <p>
     * NOTE: The behaviour applies to {@link FBField#getString()} and code paths that work through {@code getString()}
     * (this usually includes {@link FBField#getObject()}.
     * </p>
     *
     * @param trimTrailing
     *         {@code true} trim trailing spaces
     */
    void setTrimTrailing(boolean trimTrailing);

    /**
     * @return {@code true} trim trailing enabled, {@code false} trim trailing disabled
     */
    boolean isTrimTrailing();

    /**
     * Trims trailing spaces from a string {@code value}.
     *
     * @param value
     *         value to trim
     * @return value without trailing spaces, {@code null} if {@code value} was null
     */
    static @Nullable String trimTrailing(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        for (int idx = value.length() - 1; idx >= 0; idx--) {
            if (value.charAt(idx) != ' ') {
                return value.substring(0, idx + 1);
            }
        }
        return "";
    }
}
