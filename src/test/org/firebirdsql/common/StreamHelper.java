// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import java.util.stream.IntStream;

/**
 * Helpers for stream operations.
 *
 * @author Mark Rotteveel
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
