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
 * Helper for numeric values.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2.11
 */
public final class NumericHelper {

    private NumericHelper() {
        // no instances
    }

    /**
     * Returns the int as an unsigned long (no sign extension).
     *
     * @param intValue Integer value
     * @return {@code intValue} as an unsiged long.
     */
    public static long toUnsignedLong(int intValue) {
        return ((long) intValue) & 0xffffffffL;
    }

    /**
     * Checks if the supplied long would fit in an unsigned 32 bit integer.
     * <p>
     * In essence this checks if {@code longValue >= 0 && longValue <= 0xffffffffL}
     * </p>
     *
     * @param longValue Long value to check
     * @return {@code true} if the long value fits as an unsigned 32 bit value
     */
    public static boolean fitsUnsigned32BitInteger(long longValue) {
        return longValue >= 0 && longValue <= 0xffffffffL;
    }
}
