/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.field;

import org.firebirdsql.util.InternalApi;

/**
 * Trim behaviour of {@code getString} in string fields.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@InternalApi
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
    static String trimTrailing(String value) {
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
