// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.matchers;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Factory for matchers to assert comparable values.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class ComparableMatcherFactory {

    private ComparableMatcherFactory() {
        // no instances
    }

    /**
     * Creates a {@link Comparable} matcher for the specified {@code comparison}.
     *
     * @param comparison
     *         comparison to assert ({@code <}, {@code <=}, {@code ==}, {@code >=}, {@code >}, or {@code lessThan},
     *         {@code lessThanOrEqualTo}, {@code equalTo}, {@code greaterThanOrEqualTo}, {@code greaterThan})
     * @param operand
     *         operand to compare against
     * @param <T>
     *         type parameter
     * @return matcher to assert the comparison
     * @throws IllegalArgumentException
     *         for an unsupported value for {@code comparison}
     */
    public static <T extends Comparable<T>> Matcher<T> compares(String comparison, T operand) {
        return switch (comparison) {
            case "<", "lessThan" -> Matchers.lessThan(operand);
            case "<=", "lessThanOrEqualTo" -> Matchers.lessThanOrEqualTo(operand);
            case "==", "equalTo" -> Matchers.comparesEqualTo(operand);
            case ">=", "greaterThanOrEqualTo" -> Matchers.greaterThanOrEqualTo(operand);
            case ">", "greaterThan" -> Matchers.greaterThan(operand);
            default -> throw new IllegalArgumentException("Unsupported comparison: " + comparison);
        };
    }
}
