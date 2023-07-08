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

import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods for conversion to or from primitive values.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class Primitives {

    private Primitives() {
        // no instances
    }

    /**
     * Convert list of numbers into array of {@code int}.
     *
     * @param numbers
     *         list of numbers
     * @return array of {@code int}
     */
    public static int[] toIntArray(List<? extends Number> numbers) {
        int size = numbers.size();
        int[] result = new int[size];
        for (int idx = 0; idx < size; idx++) {
            result[idx] = numbers.get(idx).intValue();
        }
        return result;
    }

    /**
     * Convert list of numbers into array of {@code long}.
     *
     * @param longObjects
     *         list of numbers
     * @return array of {@code long}
     */
    public static long[] toLongArray(List<? extends Number> longObjects) {
        int size = longObjects.size();
        long[] result = new long[size];
        for (int idx = 0; idx < size; idx++) {
            result[idx] = longObjects.get(idx).longValue();
        }
        return result;
    }

    /**
     * Convert array of {@code int} to list of {@link Long}.
     *
     * @param intValues
     *         array of {@code int}
     * @return list of {@code Long}
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static List<Long> toLongList(int[] intValues) {
        List<Long> result = new ArrayList<>(intValues.length);
        for (int i = 0; i < intValues.length; i++) {
            result.add((long) intValues[i]);
        }
        return result;
    }

}
