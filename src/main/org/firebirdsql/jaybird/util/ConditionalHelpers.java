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
package org.firebirdsql.jaybird.util;

/**
 * Helpers for conditional behaviour and conditional expressions.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class ConditionalHelpers {

    private ConditionalHelpers() {
        // no instances
    }

    /**
     * Returns {@code firstValue} if it is non-zero, otherwise {@code secondValue}.
     *
     * @param firstValue
     *         first value
     * @param secondValue
     *         second value
     * @return {@code firstValue} if it is non-zero, otherwise {@code secondValue}
     */
    public static int firstNonZero(int firstValue, int secondValue) {
        if (firstValue != 0) return firstValue;
        return secondValue;
    }

    /**
     * Returns {@code firstValue} if it is non-zero, otherwise {@code secondValue} if it is non-zero, otherwise
     * {@code thirdValue}.
     *
     * @param firstValue
     *         first value
     * @param secondValue
     *         second value
     * @param thirdValue
     *         third value
     * @return {@code firstValue} if it is non-zero, otherwise {@code secondValue} if it is non-zero, otherwise
     * {@code thirdValue}
     */
    public static int firstNonZero(int firstValue, int secondValue, int thirdValue) {
        if (firstValue != 0) return firstValue;
        return firstNonZero(secondValue, thirdValue);
    }

}
