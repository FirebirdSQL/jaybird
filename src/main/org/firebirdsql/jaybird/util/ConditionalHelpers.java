// SPDX-FileCopyrightText: Copyright 2023-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.jspecify.annotations.Nullable;

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

    /**
     * Returns {@code firstValue} if it is non-null, otherwise {@code secondValue}.
     *
     * @param firstValue
     *         first value
     * @param secondValue
     *         second value
     * @return {@code firstValue} if it is non-null, otherwise {@code secondValue} (which may be {@code null})
     * @see java.util.Objects#requireNonNullElse(Object, Object)
     * @since 7
     */
    public static <T extends @Nullable Object> T firstNonNull(T firstValue, T secondValue) {
        if (firstValue != null) return firstValue;
        return secondValue;
    }

    /**
     * Returns {@code firstValue} if it is non-null, otherwise {@code secondValue} if it is non-null, otherwise
     * {@code thirdValue}.
     *
     * @param firstValue
     *         first value
     * @param secondValue
     *         second value
     * @param thirdValue
     *         third value
     * @return {@code firstValue} if it is non-null, otherwise {@code secondValue} if it is non-null, otherwise
     * {@code thirdValue} (which may be {@code null})
     * @since 7
     */
    public static <T extends @Nullable Object> T firstNonNull(T firstValue, T secondValue, T thirdValue) {
        if (firstValue != null) return firstValue;
        return firstNonNull(secondValue, thirdValue);
    }

}
