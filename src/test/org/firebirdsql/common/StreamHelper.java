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
package org.firebirdsql.common;

import java.util.stream.IntStream;

/**
 * Helpers for stream operations.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public class StreamHelper {

    /**
     * Returns an {@link IntStream} that iterates from {@code maxValue} to {@code minValue}.
     *
     * @param minValue Minimum (destination) value (inclusive)
     * @param maxValue Maximum (starting) value (inclusive)
     * @return stream over a closed range
     */
    public static IntStream reverseClosedRange(int minValue, int maxValue) {
        return IntStream.rangeClosed(minValue, maxValue)
                .map(i -> maxValue - i + minValue);
    }

}
