/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.util;

/**
 * Helper class for string operations
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class StringUtils {

    private StringUtils() {
        // no instances
    }

    /**
     * Trims {@code value} if non-null, returning the trimmed value, or {@code null} if {@code value} was {@code null}
     * or empty after trimming.
     *
     * @param value Value to trim
     * @return Trimmed string {@code value}, or {@code null} when null, or empty after trim.
     * @see String#trim() 
     */
    public static String trimToNull(String value) {
        if (value != null) {
            String newValue = value.trim();
            if (!newValue.isEmpty()) {
                return newValue;
            }
        }
        return null;
    }
}
