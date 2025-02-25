// SPDX-FileCopyrightText: Copyright 2016-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.util;

/**
 * Helper for numeric values.
 *
 * @author Mark Rotteveel
 * @since 2.2.11
 */
@InternalApi
public final class NumericHelper {

    private NumericHelper() {
        // no instances
    }

    /**
     * Returns the int as an unsigned long (no sign extension).
     *
     * @param intValue
     *         Integer value
     * @return {@code intValue} as an unsigned long.
     */
    public static long toUnsignedLong(int intValue) {
        return intValue & 0xffffffffL;
    }

    /**
     * Checks if the supplied long would fit in an unsigned 32-bit integer.
     * <p>
     * In essence this checks if {@code longValue >= 0 && longValue <= 0xffff_ffffL}
     * </p>
     *
     * @param longValue
     *         Long value to check
     * @return {@code true} if the long value fits as an unsigned 32 bit value
     */
    public static boolean fitsUnsigned32BitInteger(long longValue) {
        return longValue >= 0 && longValue <= 0xffff_ffffL;
    }

}
